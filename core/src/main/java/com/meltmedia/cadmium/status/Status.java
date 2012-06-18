package com.meltmedia.cadmium.status;

import java.util.List;

public class Status {

	public Status(String environment, String repo, String branch, String revision, String source, String maintPageState, List<StatusMember> members) {
		
		this.environment = environment;
		this.repo = repo;
		this.branch = branch;
		this.revision = revision;
		this.source = source;
		this.maintPageState = maintPageState;
        this.setMembers(members);
	}	
	
	
	private String environment;
	private String repo;
	private String branch;
	private String revision;
	private String source;
	private String maintPageState;
	private List<StatusMember> members;
	
	
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public String getRepo() {
		return repo;
	}
	public void setRepo(String repo) {
		this.repo = repo;
	}
	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}
	public String getRevision() {
		return revision;
	}
	public void setRevision(String revision) {
		this.revision = revision;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getMaintPageState() {
		return maintPageState;
	}
	@Override
	public String toString() {
		return "Status [environment=" + environment + ", repo=" + repo
				+ ", branch=" + branch + ", revision=" + revision + ", source="
				+ source + ", maintPageState=" + maintPageState + ", members="
				+ members + "]";
	}
	public void setMaintPageState(String maintPageState) {
		this.maintPageState = maintPageState;
	}
	public void setMembers(List<StatusMember> members) {
		this.members = members;
	}
	public List<StatusMember> getMembers() {
		return members;
	}
	
	
	
}
