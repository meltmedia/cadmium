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
package com.meltmedia.cadmium.cli;

import java.io.File;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.git.GitService;
import com.meltmedia.cadmium.status.Status;

@Parameters(commandDescription="This command will move all content from one branch to another or tag a version of a branch.", separators="=")
public class CloneCommand extends AbstractAuthorizedOnly implements CliCommand {

  @Parameter(description="<source-site> <target-site>", required=true)
  private List<String> sites;
  
  @Parameter(names="--repo", description="Overrides the repository url from the server.", required=false)
  private String repo;
  
  @Parameter(names={"--tag", "-t"}, description="The name of a tag to create to serve site2 from.", required=false)
  private String tagname;
  
  @Parameter(names={"--message", "-m"}, description="comment", required=true)
  private String comment;
  
  public void execute() throws Exception {
    GitService site1Service = null;
    GitService site2Service = null;    
    
    String site1 = sites.get(0);
    String site2 = sites.get(1);
    
    try{
      System.out.println("Getting status of ["+site1+"]");
      Status site1Status = StatusCommand.getSiteStatus(site1, token);
      
      System.out.println("Cloning repository that ["+site1+"] is serving");
      site1Service = cloneSiteRepo(site1Status);
      
      System.out.println("Getting status of ["+site2+"]");
      Status site2Status = StatusCommand.getSiteStatus(site2, token);
      
      String site2repo = site2Status.getRepo();
      if(repo != null) {
        site2Status.setRepo(repo);
      }
      
      System.out.println("Cloning repository that ["+site2+"] is serving");
      site2Service = cloneSiteRepo(site2Status);
      
      String revision = site1Service.getCurrentRevision();
      String branch = site2Service.getBranchName();
      
      if(site2Service.isTag(site2Status.getBranch()) && tagname == null) {
        throw new Exception("Site ["+site2+"] is currently serving from a tag. Please specify a new tag name to serve from!");
      }
      
      if(site1Status.getRepo().equals(site2repo) && site1Status.getBranch().equals(site2Status.getBranch()) && site1Status.getRevision().equals(site2Status.getRevision())) {
        throw new Exception("Nothing to do, sites are currently serving same content.");
      } 
      
      if(site1Status.getRepo().equals(site2repo) && site2Service.isTag(site2Status.getBranch()) && site1Service.isTag(site1Status.getBranch()) && !site1Status.getBranch().equals(site2Status.getBranch())) {
        System.out.println("Both sites are on tags using tag ["+site1Service.getBranchName()+"]");
        branch = site1Service.getBranchName();
        revision = null;
      } else if(site1Status.getRepo().equals(site2repo) && site2Service.isTag(site2Status.getBranch()) && !site1Status.getBranch().equals(site2Status.getBranch())) {
        System.out.println("["+site2+"] is on a tag switching to ["+site1Service.getBranchName()+"] and revision ["+site1Service.getCurrentRevision()+"] to tag from.");
        site2Service.switchBranch(site1Service.getBranchName());
        site2Service.resetToRev(site1Service.getCurrentRevision());
      } else {
        System.out.println("Cloning content from ["+site1+"] to ["+site2+"]");
        revision = cloneContent(site1Service.getBaseDirectory(), site2Service, comment);
      }
      
      if(tagname != null) {
        System.out.println("Tagging content for ["+site2+"] to serve.");
        site2Service.tag(tagname, comment);
        revision = null;
        branch = tagname;
      }
      
      System.out.println("Sending update message to ["+site2+"]");
      UpdateCommand.sendUpdateMessage(site2, branch, revision, "Cloned from ["+site1+"]: " + comment, token);
      
    } catch(Exception e) {
      e.printStackTrace();
      System.err.println("Failed to clone ["+site1+"] to ["+site2+"]: "+e.getMessage());
    } finally {
      if(site1Service != null){
        try {
          site1Service.close();
        } catch(Exception e){}
      }
      if(site2Service != null){
        try {
          site2Service.close();
        } catch(Exception e){}
      }
    }
  }

  public static String cloneContent(String source, GitService service, String comment) throws Exception {
    String rev = GitService.moveContentToBranch(source, service, service.getBranchName(), comment);
    service.push(false);
    return rev;
  }
  
  public static GitService cloneSiteRepo(Status status) throws Exception {
    File tmpDir = File.createTempFile("site", "git");
    GitService git = null;
    if(tmpDir.delete()) {
      try {
        git = GitService.cloneRepo(status.getRepo(), tmpDir.getAbsolutePath());
        if(!git.getBranchName().equals(status.getBranch())) {
          git.switchBranch(status.getBranch());
        }
        if(!git.getCurrentRevision().equals(status.getRevision())) {
          git.resetToRev(status.getRevision());
        }
      } catch(Exception e) {
        System.err.println("Failed to clone repo "+status.getRepo()+" branch "+status.getBranch()+ "["+tmpDir+"]");
        e.printStackTrace();
        if(git != null) {
          git.close();
          git = null;
        }
      }
    }
    return git;
  }

  @Override
  public String getCommandName() {
    return "clone";
  }
}
