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
package com.meltmedia.cadmium.captcha;

import net.tanesha.recaptcha.ReCaptchaImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Checks the validity of the incoming Http request with ReCaptcha.
 * 
 * @author John McEntire
 *
 */
public final class CaptchaValidator {
  public static final String CHALLENGE_FIELD_NAME = "recaptcha_challenge_field";
  public static final String RESPONSE_FIELD_NAME = "recaptcha_response_field";
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private ReCaptchaImpl reCaptcha;
  
  /**
   * Initializes an instance with the given privateKey.
   * @param privateKey
   */
  public CaptchaValidator(String privateKey) {
    reCaptcha = new ReCaptchaImpl();
    reCaptcha.setPrivateKey(privateKey);
  }
  
  /**
   * Validates the captcha response.
   * 
   * @param request
   * @return
   */
  public boolean isValid(HttpServletRequest request) {
    String challenge = request.getParameter(CHALLENGE_FIELD_NAME);
    String response = request.getParameter(RESPONSE_FIELD_NAME);    
    return isValid(request, challenge, response);
  }
  
  /**
   * Validates the captcha response.
   * 
   * @param request
   * @param captcha
   * @return
   */
  public boolean isValid(HttpServletRequest request, CaptchaRequest captcha) {
    String challenge = captcha.getRecaptcha_challenge_field();
    String response = captcha.getRecaptcha_response_field();    
    return isValid(request, challenge, response);
  }
   
  /**
   * Validates the captcha response.
   * 
   * @param request
   * @param challenge
   * @param response
   * @return
   */
  public boolean isValid(HttpServletRequest request, String challenge, String response) {
    String remoteAddr = request.getRemoteAddr();
    log.info("Challenge Field Name = [{}]", challenge);
    log.info("Response Field Name = [{}]", response);
    if(challenge != null && response != null && challenge.trim().length() > 0) {
      return reCaptcha.checkAnswer(remoteAddr, challenge, response).isValid();
    } else {
      return false;
    }
  }
}
