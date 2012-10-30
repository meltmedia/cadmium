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
package com.meltmedia.cadmium.email.jersey;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javassist.expr.NewArray;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.meltmedia.cadmium.core.ContentService;
import com.meltmedia.cadmium.email.config.EmailComponentConfiguration;
import com.meltmedia.cadmium.email.config.EmailComponentConfiguration.Field;
import com.meltmedia.cadmium.email.model.EmailForm;

public class EmailFormValidator {
	
	private static final String emailExpression = "^.+?@.+?[\\.]{1}.+?$";
  static Pattern pattern = Pattern.compile(emailExpression,Pattern.CASE_INSENSITIVE);
   
  public static void validate(HttpServletRequest request, EmailComponentConfiguration config, ContentService contentService) throws ValidationException {
  	
  	List<ValidationError> errors = new ArrayList<ValidationError>();
  	
  	// Validate FromAddress
  	String fromAddress = getRequiredFielValue(request,config.getFromAddress(),Constants.FROM_ADDRESS);
  	if(StringUtils.isBlank(fromAddress)) {
  		errors.add(new ValidationError(Constants.FROM_ADDRESS, Constants.FROM_ADDRESS + " is required."));
  	} else if(!isValidEmailAddress(fromAddress)) {
  		errors.add(new ValidationError(Constants.FROM_ADDRESS, Constants.FROM_ADDRESS + " is an invalid email address."));
  	}
  	
  	// Validate From Name
    if(StringUtils.isBlank(getRequiredFielValue(request, config.getFromName(), Constants.FROM_NAME))) {
  		errors.add(new ValidationError(Constants.FROM_NAME, Constants.FROM_NAME + " is required."));
  	}
    
    // Validate ToAddress
    String toAddress = getRequiredFielValue(request, config.getToAddress(), Constants.TO_ADDRESS);
    if(StringUtils.isBlank(toAddress)) {
  		errors.add(new ValidationError(Constants.TO_ADDRESS, Constants.TO_ADDRESS + " is required."));
  	} else if(!isValidEmailAddress(toAddress)) {
  		errors.add(new ValidationError(Constants.TO_ADDRESS, Constants.TO_ADDRESS + " is an invalid email address."));
  	}
    
    //  Validate To Name
  	if(StringUtils.isBlank(getRequiredFielValue(request, config.getToName(), Constants.TO_NAME))) {
  		errors.add(new ValidationError(Constants.TO_NAME, Constants.TO_NAME + " is required."));
  	}
  	
  	// Validate Subject
  	if(StringUtils.isBlank(getRequiredFielValue(request, config.getSubject(), Constants.SUBJECT))) {
  		errors.add(new ValidationError(Constants.SUBJECT, Constants.SUBJECT + " is required."));
  	}
  	for(Field field : config.getFields()) {
  		checkValidField(field, request, contentService, errors);
  	}
  	
	  if (errors.size() > 0) {
      throw new ValidationException("Validation error(s) occurred.", errors.toArray(new ValidationError[errors.size()]));
    }
  	
  }
  
  protected static void checkValidField(Field field, HttpServletRequest request, ContentService contentService, List<ValidationError> errors) {
  	String value = field.getRawValue(request);
  	if(field.required && StringUtils.isBlank(value)) {
  		errors.add(new ValidationError(field.name, field.validationMessage));
  	}
  	if(StringUtils.isNotBlank(value)) {
			if(field.email && !isValidEmailAddress(value)) {
				errors.add(new ValidationError(field.name, field.validationMessage));
			} else if (field.page && !pageExists(value, contentService)) {
				errors.add(new ValidationError(field.name, field.validationMessage));
			}
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
  
	protected static boolean pageExists(String pagePath, ContentService contentService) {
		if (pagePath.contains("-INF")) {
			return false;
		}
		File pagePathFile = new File(contentService.getContentRoot(),pagePath);
		return pagePathFile.exists();
	}
	
	protected static String getRequiredFielValue(HttpServletRequest request, String defaultString, String requestKey) {
		if(StringUtils.isNotBlank(defaultString)) {
			return defaultString.trim();
		} else if(StringUtils.isNotBlank(request.getParameter(requestKey))) {
			return request.getParameter(requestKey);
		}		
		return "";
	}
  
  
}
