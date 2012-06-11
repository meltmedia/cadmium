package com.meltmedia.cadmium.cli;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.git.GitService;
import com.meltmedia.cadmium.status.Status;

@Parameters(commandDescription="Commits content from a specific directory into a repo:branch that a site is serving from then updates the site to serve the new content.", separators="=")
public class CommitCommand extends AbstractAuthorizedOnly implements CliCommand {

  @Parameter(names="--content-directory", description="The directory to pull content from.", required=false)
  private String content = ".";
  
  @Parameter(names="--site", description="The site to pull status from.", required=true)
  private String site;
  
  @Parameter(names="--repo", description="Overrides the repository url from the server.", required=false)
  private String repo;
  
  @Parameter(description="comment", required=true)
  private List<String> commentLines;
  private String comment;
  
  public void execute() throws Exception {

    for(String comment : commentLines) {
      if(this.comment == null) {
        this.comment = "";
      } else {
        this.comment += " ";
      }
      this.comment += comment;
    }
    
    GitService git = null;
    try {
      System.out.println("Getting status of ["+site+"]");
      Status status = CloneCommand.getSiteStatus(site, token);
  
      if(repo != null) {
        status.setRepo(repo);
      }
      
      System.out.println("Cloning repository that ["+site+"] is serving");
      git = CloneCommand.cloneSiteRepo(status);
      
      String revision = status.getRevision();
      String branch = status.getBranch();
      
      if(git.isTag(branch)) {
        throw new Exception("Cannot commit to a tag!");
      }
      System.out.println("Cloning content from ["+content+"] to ["+site+":"+branch+"]");
      revision = CloneCommand.cloneContent(content, git, comment);
      
      System.out.println("Switching content on ["+site+"]");
      CloneCommand.sendUpdateMessage(site, branch, revision, comment, token);
      
    } catch(Exception e) {
      System.err.println("Failed to commit changes to ["+site+"]: "+e.getMessage());
    } finally {
      if(git != null) {
        git.close();
      }
    }
    
  }

  @Override
  public String getCommandName() {
    return "commit";
  }
}
