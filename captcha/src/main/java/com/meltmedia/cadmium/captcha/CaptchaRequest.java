package com.meltmedia.cadmium.captcha;

public interface CaptchaRequest {
  public String getRecaptcha_challenge_field();
  public String getRecaptcha_response_field();
}
