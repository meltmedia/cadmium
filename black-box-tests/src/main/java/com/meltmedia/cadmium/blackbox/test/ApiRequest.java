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
package com.meltmedia.cadmium.blackbox.test;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;

/**
 * Class to simplify rest requests.
 */
public class ApiRequest {
  public static enum Method {GET,POST,PUT,DELETE};
  private String url;
  private Method httpMethod;
  private Map<String, String> headers;
  private Object postBody;
  private String postContentType;

  public ApiRequest(String url, Method httpMethod, Map<String, String> headers, Object postBody, String postContentType) {
    this.url = url;
    this.httpMethod = httpMethod;
    this.headers = headers;
    this.postBody = postBody;
    this.postContentType = postContentType;
  }

  public ApiRequest(String url, Method httpMethod, Map<String, String> headers) {
    this(url, httpMethod, headers, null, null);
  }

  public ApiRequest(String url, Method httpMethod) {
    this(url, httpMethod, null, null, null);
  }

  public HttpResponse makeRequest() throws Exception {
    System.out.println("Making "+httpMethod+" request to "+url);
    DefaultHttpClient client = new DefaultHttpClient();

    HttpUriRequest message = null;
    switch(httpMethod) {
      case POST:
        message = setupPostRequest();
        break;
      case PUT:
        message = setupPutRequest();
        break;
      case DELETE:
        message = setupDeleteRequest();
        break;
      case GET:
      default:
        message = setupGetRequest();
        break;
    }
    addPostBody(message);
    setHeadersIfAny(message);
    return client.execute(message);
  }

  private void setHeadersIfAny(HttpUriRequest request) {
    if(httpMethod != Method.GET && httpMethod != Method.DELETE && postContentType != null) {
      request.setHeader("Content-Type", postContentType);
    }
    if(headers != null && !headers.isEmpty()) {
      for(String header : headers.keySet()) {
        String value = headers.get(header);
        request.setHeader(header, value);
      }
    }
  }

  private void addPostBody(HttpUriRequest request) throws Exception {
    String postContent = null;
    if(postBody != null) {
      if(postBody instanceof String) {
        postContent = (String)postBody;
      } else if(postBody instanceof Map && (postContentType == null || !postContentType.equals("application/json"))) {
        Map<?,?> postBodyMap = (Map<?,?>)postBody;
        if(!postBodyMap.isEmpty()) {
          postContent = "";
          for(Object key : postBodyMap.keySet()) {
            Object value = postBodyMap.get(key);
            if(value instanceof Collection) {
              Collection<?> values = (Collection<?>)value;
              for(Object val : values) {
                if (postContent.length() > 0) {
                  postContent += "&";
                }
                postContent += URLEncoder.encode(key.toString(), "UTF-8");
                postContent += "=" + URLEncoder.encode(val.toString(), "UTF-8");
              }
            } else {
              if (postContent.length() > 0) {
                postContent += "&";
              }
              postContent += URLEncoder.encode(key.toString(), "UTF-8");
              postContent += "="+URLEncoder.encode(value.toString(), "UTF-8");
            }
          }
        }
        if(postContentType == null) {
          postContentType = "application/x-www-form-urlencoded";
        }
      } else {
        postContent = new Gson().toJson(postBody);
      }
      if(postContent != null && request instanceof HttpEntityEnclosingRequestBase) {
        System.out.println("Posting body: " + postContent);
        HttpEntityEnclosingRequestBase entityBasedRequest = (HttpEntityEnclosingRequestBase)request;
        StringEntity entity = new StringEntity(postContent);
        entityBasedRequest.setEntity(entity);
        if(postContentType != null) {
          entity.setContentType(postContentType);
        }
      }
    }
  }

  private HttpGet setupGetRequest() {
    return new HttpGet(url);
  }

  private HttpPut setupPutRequest() {
    return new HttpPut(url);
  }

  private HttpDelete setupDeleteRequest() {
    return new HttpDelete(url);
  }

  private HttpPost setupPostRequest() {
    return new HttpPost(url);
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Method getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(Method httpMethod) {
    this.httpMethod = httpMethod;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public Object getPostBody() {
    return postBody;
  }

  public void setPostBody(Object postBody) {
    this.postBody = postBody;
  }

  public String getPostContentType() {
    return postContentType;
  }

  public void setPostContentType(String postContentType) {
    this.postContentType = postContentType;
  }
}
