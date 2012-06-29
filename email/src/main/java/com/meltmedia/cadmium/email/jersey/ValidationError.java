package com.meltmedia.cadmium.email.jersey;

public class ValidationError {
	
	private String fieldName;
  private String message;
  
  public ValidationError(String fieldName, String message) {
    super();
    this.fieldName = fieldName;
    this.message = message;
  }

  /**
   * @return the fieldName
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * @param fieldName the fieldName to set
   */
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }

}
