package com.meltmedia.cadmium.email.jersey;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.meltmedia.cadmium.email.model.EmailForm;

public class EmailFormValidator {
	
	private static final String emailExpression = "^.+?@.+?[\\.]{1}.+?$";
  static Pattern pattern = Pattern.compile(emailExpression,Pattern.CASE_INSENSITIVE);
  
  public static void validate(EmailForm emailForm) throws ValidationException {
  	ArrayList<ValidationError> errors = new ArrayList<ValidationError>();
  	
  	if (!isValidEmailAddress(emailForm.getToAddress())) {
    	errors.add(new ValidationError(Constants.TO_ADDRESS,"Please Use a Valid To Email Address."));
    }
  	
  	if (!isValidEmailAddress(emailForm.getFromAddress())) {
    	errors.add(new ValidationError(Constants.FROM_ADDRESS,"Please Use a Valid From Email Address."));
    }
  	
  	if (StringUtils.isEmpty(emailForm.getToName())) {
  		errors.add(new ValidationError(Constants.TO_NAME, "To Name is required."));
  	}
  	
  	if (StringUtils.isEmpty(emailForm.getFromName())) {
  		errors.add(new ValidationError(Constants.FROM_NAME, "From Name is required."));
  	}
  	
  	if (StringUtils.isEmpty(emailForm.getMessage())) {
  		errors.add(new ValidationError(Constants.MESSAGE, "message is required."));
  	}
  	
  	if (StringUtils.isEmpty(emailForm.getPagePath())) {
  		errors.add(new ValidationError(Constants.PAGE_PATH, "Page Path is required."));
  	}
  	
  	if (StringUtils.isEmpty(emailForm.getSubject())) {
  		errors.add(new ValidationError(Constants.SUBJECT, "Page Path is required."));
  	}
  	
  	 if (errors.size() > 0) {
       throw new ValidationException("Validation error(s) occurred.", errors.toArray(new ValidationError[errors.size()]));
     }
  	
  }
  
  protected static boolean isValidEmailAddress(String emailAddress) {
    
    if (emailAddress != null) {
      if (pattern.matcher(emailAddress).matches() && !emailAddress.contains("'") && !emailAddress.contains(")") && !emailAddress.contains("*") && !emailAddress.contains("\"")) {
        return true;
      }
    }   
    return false;
  }
  
  
}
