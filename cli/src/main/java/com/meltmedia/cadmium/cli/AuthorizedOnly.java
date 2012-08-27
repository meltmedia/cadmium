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
package com.meltmedia.cadmium.cli;

/**
 * The interface to implement in order to tell {@link CadmiumCli} instance 
 * that the command that implements this interface should be authenticated 
 * in the Github API. The {@link AbstractAuthorizedOnly} class should be 
 * extended as it already has implementations of each of the methods 
 * required by this interface and provides helper methods that can be used 
 * in authentication purposes.
 * 
 * @author John McEntire
 *
 */
public interface AuthorizedOnly {
  
  /**
   * Sets the Github API token for the current instance.
   * 
   * @param token 
   */
  public void setToken(String token);
  
  /**
   * @return The Github API token for the current instance.
   */
  public String getToken();
  

  /**
   * Tells the {@link CadmiumCli} instance that this command should silence the authentication and just fail if not authorized.
   * 
   * @return true if the authentication should not prompt for a username and password.
   */
  public boolean isAuthQuiet();
}
