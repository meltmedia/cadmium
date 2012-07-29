package com.meltmedia.cadmium.servlets;

import java.io.IOException;

@SuppressWarnings("serial")
public class UnsupportedProtocolException extends IOException {

  public UnsupportedProtocolException() {
    super();
  }

  public UnsupportedProtocolException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public UnsupportedProtocolException(String arg0) {
    super(arg0);
  }

  public UnsupportedProtocolException(Throwable arg0) {
    super(arg0);
  }

}
