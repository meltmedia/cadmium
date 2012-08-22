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

import javax.mail.Session;

/**
 * A strategy for accessing the java mail session.
 * 
 * There must be a constructor that takes no arguments for it to be used
 */
public interface SessionStrategy
{
  public Session getSession()
    throws EmailException;
  
  public void configure(Dictionary<String, Object> config)
    throws EmailException;
}
