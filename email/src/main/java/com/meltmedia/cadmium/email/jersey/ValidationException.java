package com.meltmedia.cadmium.email.jersey;

public class ValidationException extends Exception {
	private static final long serialVersionUID = 1L;

	private ValidationError[] errors;

	public ValidationException() {
	    super();
	  }

	  public ValidationException(String message, Throwable cause) {
	    super(message, cause);
	  }

	  public ValidationException(String message) {
	    super(message);
	  }

	  public ValidationException(Throwable cause) {
	    super(cause);
	  }

	  public ValidationException(String message, ValidationError[] errors) {
	    super(message);
	    this.errors = errors;
	  }

	  public ValidationError[] getErrors() {
	    return errors;
	  }
}
