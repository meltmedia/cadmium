package com.meltmedia.cadmium.core.meta;

public class MimeType {
  private String extension;
  private String contentType;
  
  public MimeType(){}

  public String getExtension() {
    return extension;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
}
