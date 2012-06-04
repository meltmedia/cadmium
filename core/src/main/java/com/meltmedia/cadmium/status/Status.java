package com.meltmedia.cadmium.status;

public class Status {

	public Status(String environment, String repo, String branch, String revision, String sourceRepo, String sourceBranch, String sourceRevision, String maintPageState) {
		
		this.environment = environment;
		this.repo = repo;
		this.branch = branch;
		this.revision = revision;
		this.sourceRepo = sourceRepo;
		this.sourceBranch = sourceBranch;
		this.sourceRevision = sourceRevision;
		this.maintPageState = maintPageState;
	}	
	
	
	private String environment;
	private String repo;
	private String branch;
	private String revision;
	private String sourceRepo;
	private String sourceBranch;
	private String sourceRevision;
	private String maintPageState;
	
	
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
	public String getSourceRepo() {
		return sourceRepo;
	}
	public void setSourceRepo(String sourceRepo) {
		this.sourceRepo = sourceRepo;
	}
	public String getSourceBranch() {
		return sourceBranch;
	}
	public void setSourceBranch(String sourceBranch) {
		this.sourceBranch = sourceBranch;
	}
	public String getSourceRevision() {
		return sourceRevision;
	}
	public void setSourceRevision(String sourceRevision) {
		this.sourceRevision = sourceRevision;
	}
	public String getMaintPageState() {
		return maintPageState;
	}
	public void setMaintPageState(String maintPageState) {
		this.maintPageState = maintPageState;
	}
	@Override
	public String toString() {
		return "Status [environment=" + environment + ", repo=" + repo
				+ ", branch=" + branch + ", revision=" + revision
				+ ", sourceRepo=" + sourceRepo + ", sourceBranch="
				+ sourceBranch + ", sourceRevision=" + sourceRevision
				+ ", maintPageState=" + maintPageState + "]";
	}
	
	
}
