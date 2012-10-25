/**
 *    Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.core.config.impl;

import java.util.List;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.resolver.Resolver;

/**
 * This is an implementation of a SnakeYaml Constructor that will parse an unknown tagged node into a Java Map.
 * 
 * @author John McEntire
 *
 */
public class YamlLenientConstructor extends Constructor {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private Set<Tag> tagsDefined = new HashSet<Tag>();
  private Resolver resolver = new Resolver();
  
  /**
   * Overrides what is mapped to tags with respect to default action to take on unknown tags.
   */
  public YamlLenientConstructor() {
    super();
  }

  /**
   * Overridden to capture what tags are defined specially.
   */
  @Override
  public TypeDescription addTypeDescription(TypeDescription definition) {
    if(definition != null && definition.getTag() != null) {
      tagsDefined.add(definition.getTag());
    }
    return super.addTypeDescription(definition);
  }

  /**
   * Overridden to fetch constructor even if tag is not mapped.
   */
  @Override
  protected Construct getConstructor(Node node) {
    Construct construct = super.getConstructor(node);
    logger.trace("getting constructor for node {} Tag {} = {}", new Object[] {node, node.getTag(), construct});
    if(construct instanceof ConstructYamlObject && !tagsDefined.contains(node.getTag())) {
      try {
        node.getTag().getClassName();
      } catch(YAMLException e) {
        node.setUseClassConstructor(true);
        String value = null;
        if(node.getNodeId() == NodeId.scalar) {
          value = ((ScalarNode)node).getValue();
        }
        node.setTag(resolver.resolve(node.getNodeId(), value, true));
        construct = super.getConstructor(node);
        try {
          resolveType(node);
        } catch (ClassNotFoundException e1) {
          logger.debug("Could not find class.", e1);
        }
      }
    }
    
    logger.trace("returning constructor for node {} type {} Tag {} = {}", new Object[] {node, node.getType(), node.getTag(), construct});
    return construct;
  }

  /**
   * Resolves the type of a node after the tag gets re-resolved.
   * 
   * @param node
   * @throws ClassNotFoundException
   */
  private void resolveType(Node node) throws ClassNotFoundException {
    String typeName = node.getTag().getClassName();
    if(typeName.equals("int")) {
      node.setType(Integer.TYPE);
    } else if(typeName.equals("float")) {
      node.setType(Float.TYPE);
    } else if(typeName.equals("double")) {
      node.setType(Double.TYPE);
    } else if(typeName.equals("bool")) {
      node.setType(Boolean.TYPE);
    } else if(typeName.equals("date")) {
      node.setType(Date.class);
    } else if(typeName.equals("seq")) {
      node.setType(List.class);
    } else if(typeName.equals("str")) {
      node.setType(String.class);
    } else if(typeName.equals("map")) {
      node.setType(Map.class);
    } else {
      node.setType(getClassForName(node.getTag().getClassName()));
    }
  }
}
