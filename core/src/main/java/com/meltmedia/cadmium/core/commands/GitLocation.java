package com.meltmedia.cadmium.core.commands;

/**
 * <p>
 * The location of content in some git repository.  The location includes the repository URL, the branch name, and the revision number.
 * </p>
 * <p>
 * NOTE: The gitrevisions manual page refers to the branch as refname and specifies rules for picking branches and tags.  Perhaps
 * we should use their names in a future version.
 * </p>
 * 
 * @author Christian Trimble
 */
public class GitLocation {
  protected String branch;
  protected String repository;
  protected String revision;
  public GitLocation() {
    this(null, null, null);
  }
  public GitLocation( String repository, String branch, String commit ) {
    this.repository = repository;
    this.branch = branch;
    this.revision = commit;
  }
  public GitLocation( GitLocation toCopy ) {
    this.repository = toCopy.repository;
    this.branch = toCopy.branch;
    this.revision = toCopy.revision;
  }
  public String getBranch() {
    return branch;
  }
  public void setBranch(String branch) {
    this.branch = branch;
  }
  public String getRepository() {
    return repository;
  }
  public void setRepository(String repository) {
    this.repository = repository;
  }
  public String getRevision() {
    return revision;
  }
  public void setRevision(String revision) {
    this.revision = revision;
  }
}
