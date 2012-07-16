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
