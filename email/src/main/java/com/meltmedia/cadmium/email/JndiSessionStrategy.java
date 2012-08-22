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

import java.util.Dictionary;
import java.util.Iterator;
import java.util.Map;

import javax.mail.Session;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A session strategy that looks up email session from jndi.
 */
public class JndiSessionStrategy implements SessionStrategy {
  public static final String JNDI_CONFIG = "com.meltmedia.email.jndi";
  protected String jndiName = null;
	private Logger log = LoggerFactory.getLogger(getClass());

  public JndiSessionStrategy() {

  }

  public void configure(Dictionary<String, Object> config)
      throws EmailException {
    this.jndiName = config.get(JNDI_CONFIG).toString();
  }

  public Session getSession() throws EmailException {
    Session session = null;
    try {
      // get the session.
    	InitialContext initialContext = new InitialContext();
    	printMap(initialContext.getEnvironment());
      session = (Session)initialContext().lookup(jndiName);
    }
    catch( Exception e ) {
    	log.info(e.getMessage());
      throw new EmailException("Exception caught while looking up email session with jndi name '"+jndiName+"'.", e);
    }

    if( session == null ) {
      throw new EmailException("Could not look up email session with jndi name '"+jndiName+"'.");
    }

    return session;
  }

  public InitialContext initialContext() throws Exception {
    return new InitialContext();
  }
  
  public void printMap(Map mp) {
    Iterator it = mp.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry)it.next();
        System.out.println(pairs.getKey() + " = " + pairs.getValue());
        it.remove(); // avoids a ConcurrentModificationException
    }
}

}
