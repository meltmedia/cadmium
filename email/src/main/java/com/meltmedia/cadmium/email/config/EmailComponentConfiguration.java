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
package com.meltmedia.cadmium.email.config;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
/**
 * @author John mcEntire
 * @author chaley
 * 
 * example of Config
 * 
 * subject: 'Thought you would like this.'
 * fields:
 *  - name              : toName
 *  - name              : replyToAddress
 *    validationMessage : 'Please enter a from address'
 *    email             : True
 *  - name              : fromName
 *  - name              : message
 *  - name              : path
 *    validationMessage : 'Unable to find page'
 *    required          : True
 *    page              : True
 * fromAddress: 'first.last@domain.com'
 *
 */
public class EmailComponentConfiguration {
	
	private Set<Field> fields;
	private String fromAddress;
	private String fromName;
	private String toAddress;
	private String toName;
	private String subject;
	private Boolean useCaptcha;
		
	public Set<Field> getFields() {
		return fields;
	}

	public void setFields(Set<Field> fields) {
		this.fields = fields;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getToName() {
		return toName;
	}

	public void setToName(String toName) {
		this.toName = toName;
	}

	public Boolean getUseCaptcha() {
    return useCaptcha;
  }

  public void setUseCaptcha(Boolean useCaptcha) {
    this.useCaptcha = useCaptcha;
  }

  public static class Field {
		public String name;
		public String validationMessage;
		public boolean required;
		public boolean email;
		public boolean page;
		
		public String getValue(HttpServletRequest request, MultivaluedMap<String, String> formData) {
			if(page) {
				return "http://" + request.getServerName() + "/"  + getRawValue(formData);
			} else {
				return getRawValue(formData);
			}
		}
		
		public String getRawValue(MultivaluedMap<String, String> formData) {
			if(formData.get(name) != null) {
			  return formData.get(name).get(0);
			} else {
				return "";
			}
		}
	}
}


