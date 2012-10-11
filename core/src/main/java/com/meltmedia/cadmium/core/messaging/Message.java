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

public class Message<B> {
  private Header header;
  private B body;
  
  public Message( String command, B body ) {
    this.header = new Header(command);
    this.body = body;
  }
  
  public Message(Header header, B body) {
    this.header = header;
    this.body = body;
  }

  public Message() {
  }

  public void setHeader( Header header ) {
    this.header = header;
  }
  
  public Header getHeader() {
    return this.header;
  }
  
  public B getBody() {
    return body;
  }
  
  public void setBody( B body ) {
    this.body = body;
  }
}

