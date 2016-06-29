package com.meltmedia.cadmium.servlets.jersey.error;

/**
 * Model representing generic error message for API
 */
public class GenericError {

  /**
   * Message associated with error
   */
  String message;

  /**
   * Generic code associated with error
   */
  String code;

  /**
   * Option error stacktrace
   */
  String stack;

  /**
   * Resource path that resulted in this error
   */
  String path;

  /**
   * Default constructor
   */
  public GenericError() {}

  /**
   * Constructor using most commonly using fields (message, code, path)
   * @param message
   * @param code
   * @param path
   */
  public GenericError(String message, String code, String path) {
    this.message = message;
    this.code = code;
    this.path = path;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getStack() {
    return stack;
  }

  public void setStack(String stack) {
    this.stack = stack;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public String toString() {
    return "GenericError{" +
        "message='" + message + '\'' +
        ", code='" + code + '\'' +
        ", stack='" + stack + '\'' +
        ", path='" + path + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GenericError)) return false;

    GenericError that = (GenericError) o;

    if (getMessage() != null ? !getMessage().equals(that.getMessage()) : that.getMessage() != null) return false;
    if (getCode() != null ? !getCode().equals(that.getCode()) : that.getCode() != null) return false;
    if (getStack() != null ? !getStack().equals(that.getStack()) : that.getStack() != null) return false;
    return getPath() != null ? getPath().equals(that.getPath()) : that.getPath() == null;

  }

  @Override
  public int hashCode() {
    int result = getMessage() != null ? getMessage().hashCode() : 0;
    result = 31 * result + (getCode() != null ? getCode().hashCode() : 0);
    result = 31 * result + (getStack() != null ? getStack().hashCode() : 0);
    result = 31 * result + (getPath() != null ? getPath().hashCode() : 0);
    return result;
  }
}
