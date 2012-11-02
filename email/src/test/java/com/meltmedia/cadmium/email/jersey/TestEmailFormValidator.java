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

import java.util.Arrays;
import java.util.HashSet;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Assert;
import static org.mockito.Mockito.*;
import org.junit.Test;

import com.meltmedia.cadmium.core.ContentService;
import com.meltmedia.cadmium.email.config.EmailComponentConfiguration;
import com.meltmedia.cadmium.email.config.EmailComponentConfiguration.Field;

public class TestEmailFormValidator  extends EmailFormValidator {
	
	
	
	@Test
	public void testEmailForm() {
		ContentService service = mock(ContentService.class);
		when(service.getContentRoot()).thenReturn("target/classes");
		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> request = mock(MultivaluedMap.class);
		when(request.get("path")).thenReturn(Arrays.asList("index.html"));
		System.out.println(request.get("path").get(0));
		EmailComponentConfiguration emailConfig = new EmailComponentConfiguration();
		emailConfig.setFromAddress("test.haha@domain.com");
		emailConfig.setFromName("From");
		emailConfig.setToAddress("toAddress@domain.com");
		emailConfig.setToName("To");
		emailConfig.setSubject("subject");
		emailConfig.setFields(new HashSet<Field>());
		
		// Positive Test
		try {
			validate(request,emailConfig,service);
		} catch (ValidationException e) {
			
		}
		
		// Negative Tests
		emailConfig.setToName("");
		try {
			validate(request,emailConfig,service);
		} catch (ValidationException e) {
			Assert.assertEquals("Expected Fail", 1, e.getErrors().length);
		}
	
	}
	
	@Test
	public void testWithoutConfigValues() {
		ContentService service = mock(ContentService.class);
		when(service.getContentRoot()).thenReturn("target/classes");
		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> request = mock(MultivaluedMap.class);
		EmailComponentConfiguration emailConfig = new EmailComponentConfiguration();
		emailConfig.setFields(new HashSet<Field>());
		when(request.get("dir")).thenReturn(Arrays.asList("email"));
		when(request.get("toName")).thenReturn(Arrays.asList("chris"));
		when(request.get("toAddress")).thenReturn(Arrays.asList("test.test@domain.com"));
		when(request.get("fromName")).thenReturn(Arrays.asList("Bob"));
		when(request.get("fromAddress")).thenReturn(Arrays.asList("test.test@domain.com"));
		
	// Positive Test
			try {
				validate(request,emailConfig,service);
			} catch (ValidationException e) {
				
			}
		
	}
	
	
	
}
