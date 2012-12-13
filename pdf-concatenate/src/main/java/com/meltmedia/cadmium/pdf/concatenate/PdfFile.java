package com.meltmedia.cadmium.pdf.concatenate;

import java.net.URL;

public class PdfFile {

  private URL url;
  private String password;

  public PdfFile(URL url, String password) {
    this.url = url;
    this.password = password;
  }

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
	
}
