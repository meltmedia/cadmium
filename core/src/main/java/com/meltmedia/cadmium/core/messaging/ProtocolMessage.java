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
package com.meltmedia.cadmium.core.messaging;

public final class ProtocolMessage {
  private ProtocolMessage() {}

  public static String UPDATE = "UPDATE";
  public static String UPDATE_DONE = "UPDATE_DONE";
  public static String UPDATE_FAILED = "UPDATE_FAILED";
  public static String CONFIG_UPDATE = "CONFIG_UPDATE";
  public static String CONFIG_UPDATE_DONE = "CONFIG_UPDATE_DONE";
  public static String CONFIG_UPDATE_FAILED = "CONFIG_UPDATE_FAILED";
  
  public static String CURRENT_STATE = "CURRENT_STATE";
  public static String STATE_UPDATE = "STATE_UPDATE";
  public static String SYNC = "SYNC";
  public static String HISTORY_REQUEST = "HISTORY_REQUEST";
  public static String HISTORY_RESPONSE = "HISTORY_RESPONSE";
  public static String MAINTENANCE = "MAINTENANCE";
  
  public static String API_ENDPOINT_ACCESS = "API_ENDPOINT_ACCESS";
}
