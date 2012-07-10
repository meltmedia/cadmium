package com.meltmedia.cadmium.email.jersey;

import org.junit.Assert;
import org.junit.Test;

import com.meltmedia.cadmium.email.model.EmailForm;

public class TestEmailFormValidator  extends EmailFormValidator {

	@Test
	public void testEmailForm() {
		EmailForm emailForm = new EmailForm();
		emailForm.setFromAddress("test.haha@domain.com");
		emailForm.setFromName("From");
		emailForm.setMessage("Message");
		emailForm.setPagePath("hcp/index.html");
		emailForm.setToAddress("toAddress@domain.com");
		emailForm.setToName("To");
		emailForm.setSubject("subject");
		
		// Positive Test
		try {
			validate(emailForm);
		} catch (ValidationException e) {
			
		}
		
		// Negative Tests
		emailForm.setFromAddress("Invalid Address");
		try {
			validate(emailForm);
		} catch (ValidationException e) {
			Assert.assertEquals("Expected Fail", 1, e.getErrors().length);
			Assert.assertEquals("Message check", "Please Use a Valid From Email Address.", e.getErrors()[0].getMessage());
		}
		emailForm.setToName("");
		try {
			validate(emailForm);
		} catch (ValidationException e) {
			Assert.assertEquals("Expected Fail", 2, e.getErrors().length);
		}
	
	}
	
}
