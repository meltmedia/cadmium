package com.meltmedia.cadmium.email.model;

public class EmailForm {
	
	private String toName;
	private String toAddress;
	private String fromName;
	private String fromAddress;
	private String message;
	private String pagePath;
	private String subject;
	
	public EmailForm() {
		this.toName = "";
		this.toAddress = "";
		this.fromName = "";
		this.fromAddress = "";
		this.message = "";
		this.pagePath = "";
		this.subject = "";
	}
	
	public EmailForm(String toName, String toAddress, String fromName,
			String fromAddress, String message, String pagePath,String subject) {
		this.toName = toName;
		this.toAddress = toAddress;
		this.fromName = fromName;
		this.fromAddress = fromAddress;
		this.message = message;
		this.pagePath = pagePath;
		this.subject = subject;
	}
	public String getToName() {
		return toName;
	}
	public void setToName(String toName) {
		this.toName = toName;
	}
	public String getToAddress() {
		return toAddress;
	}
	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}
	public String getFromName() {
		return fromName;
	}
	public void setFromName(String fromName) {
		this.fromName = fromName;
	}
	public String getFromAddress() {
		return fromAddress;
	}
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getPagePath() {
		return pagePath;
	}
	public void setPagePath(String pagePath) {
		this.pagePath = pagePath;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
}
