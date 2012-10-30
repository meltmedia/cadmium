package com.meltmedia.cadmium.email.config;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class EmailComponentConfiguration {
	
	private Set<Field> fields;
	private String fromAddress;
	private String fromName;
	private String toAddress;
	private String toName;
	private String subject;
		
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

	public static class Field {
		public String name;
		public String validationMessage;
		public boolean required;
		public boolean email;
		public boolean page;
		
		public String getValue(HttpServletRequest request) {
			if(page) {
				return "http://" + request.getServerName() + "/"  + getRawValue(request);
			} else {
				return getRawValue(request);
			}
		}
		
		public String getRawValue(HttpServletRequest request) {
			return request.getParameter(name);
		}
	}
}


