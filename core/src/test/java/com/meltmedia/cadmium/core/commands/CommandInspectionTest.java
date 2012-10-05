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
package com.meltmedia.cadmium.core.commands;

import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.Test;

import com.meltmedia.cadmium.core.CommandAction;

/**
 * Tests that confirm how Apache's TypeUtils should be used.
 * 
 * @author Christian Trimble
 *
 */
public class CommandInspectionTest {

  /**
   * Tests the use of TypeUtils.getTypeArguments to lookup the type assigned to a generic.
   */
  @Test
  public void testTypeUtilsGetTypeArguments() {
    Class<?> commandClass = new HistoryRequestCommandAction().getClass();
    Map<TypeVariable<?>,Type> genericMap = TypeUtils.getTypeArguments(commandClass, CommandAction.class);
    assertNotNull("The generic map was null.", genericMap);
    assertEquals("The genric map has an unexpected number of entries.", 1, genericMap.entrySet().size());
    TypeVariable<?> parameterVariable = CommandAction.class.getTypeParameters()[0];
    assertNotNull("The parameter variable from command action is null.", parameterVariable);
    Type bodyType = genericMap.get(parameterVariable);
    assertEquals("The type for history request was wrong.", HistoryRequest.class, bodyType);
  }

}
