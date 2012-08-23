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
package com.meltmedia.cadmium.email;

public class EmailLifecycle {}

//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;
//
//import org.apache.commons.lang.StringUtils;
//import org.xchain.framework.lifecycle.ConfigDocumentContext;
//import org.xchain.framework.lifecycle.LifecycleClass;
//import org.xchain.framework.lifecycle.LifecycleContext;
//import org.xchain.framework.lifecycle.LifecycleException;
//import org.xchain.framework.lifecycle.StartStep;
//
//import com.meltmedia.xchain.namespace.email.EmailConstants;
//
//import static org.apache.commons.lang.StringUtils.isEmpty;
//
//@LifecycleClass(uri = EmailConstants.URI)
//public class EmailLifecycle {
//  private static final String MODE_TEST = "test";
//  private static final String MODE_PRODUCTION = "production";
//  private static final Set<String> MODES = new HashSet<String>(Arrays.asList(new String[] {MODE_TEST, MODE_PRODUCTION}));
//  private static final String MODE_DEFAULT = MODE_TEST;
//  
//  @StartStep(localName = "config", after = { "{http://www.xchain.org/framework/lifecycle}config" }, xmlns = { "xmlns:config='" + EmailConstants.URI + "'" })
//  public static void startConfig(LifecycleContext context, ConfigDocumentContext configDocContext) throws LifecycleException {
//    String modeValue = getConfigValue(configDocContext, "/*/config:mode", MODE_DEFAULT);
//    assertValidMode(modeValue);
//    
//    String jndiNameValue = getConfigValue(configDocContext, "/*/config:jndi-name");
//    String filterValue = getConfigValue(configDocContext, "/*/config:service-filter");
//    
//    SessionStrategy strategy = null;
//    MessageTransformer transformer = null;
//    if( MODE_TEST.equals(modeValue) ) {
//      strategy = new TestingSessionStrategy();
//      transformer = new TestingMessageTransformer();
//      
//    } else if( MODE_PRODUCTION.equals(modeValue) ) {
//      if( !isEmpty(jndiNameValue) ) {
//        strategy = new JndiSessionStrategy();
//        ((JndiSessionStrategy)strategy).setJndiName(jndiNameValue);
//      } else if( !isEmpty(filterValue) ) {
//        strategy = new OSGiSessionStrategy(filterValue);
//      }
//      transformer = new IdentityMessageTransformer();
//    }
//    
//    EmailService.getInstance().setSessionStrategy(strategy);
//    EmailService.getInstance().setMessageTransformer(transformer);
//  }    
//
//  private static String getConfigValue(ConfigDocumentContext config, String path) {
//    return (String)config.getValue(path, String.class);
//  }
//  
//  private static String getConfigValue(ConfigDocumentContext config, String path, String defaultValue) {
//    String value = (String)config.getValue(path, String.class);
//    return (value == null || "".equals(value))? defaultValue : value;
//  }
//  
//  private static void assertValidMode(String mode) throws LifecycleException {
//    if( !MODES.contains(mode) )
//      throw new LifecycleException(String.format(
//          "Invalid mode '%s' set. Must be one of %s.", mode, MODES.toString()
//      )); 
//  }
//}
