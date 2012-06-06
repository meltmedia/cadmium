package com.meltmedia.cadmium.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.git.GitService;
import com.meltmedia.cadmium.status.Status;

@Parameters(commandDescription="This command will move all content from one branch to another or tag a version of a branch.", separators="=")
public class CloneCommand {
  public static final String UPDATE_ENDPOINT = "/system/update";

  @Parameter(names="--site1", description="The url to the site to clone from.", required=true)
  private String site1;

  @Parameter(names="--site2", description="The url to the site to clone to.", required=true)
  private String site2;
  
  @Parameter(names="--tagname", description="The name of a tag to create to serve site2 from.", required=false)
  private String tagname;
  
  @Parameter(description="comment", required=true)
  private String comment;
  
  public void execute() throws Exception {
    GitService site1Service = null;
    GitService site2Service = null;
    try{
      System.out.println("Getting status of ["+site1+"]");
      Status site1Status = getSiteStatus(site1);
      
      System.out.println("Cloning repository that ["+site1+"] is serving");
      site1Service = cloneSiteRepo(site1Status);
      
      System.out.println("Getting status of ["+site2+"]");
      Status site2Status = getSiteStatus(site2);
      
      System.out.println("Cloning repository that ["+site2+"] is serving");
      site2Service = cloneSiteRepo(site2Status);
      
      String revision = site1Service.getCurrentRevision();
      String branch = site2Service.getBranchName();
      
      if(site2Service.isTag(site2Status.getBranch()) && tagname == null) {
        throw new Exception("Site ["+site2+"] is currently serving from a tag. Please specify a new tag name to serve from!");
      }
      
      if(site1Status.getRepo().equals(site2Status.getRepo()) && site1Status.getBranch().equals(site2Status.getBranch()) && site1Status.getRevision().equals(site2Status.getRevision())) {
        throw new Exception("Nothing to do, sites are currently serving same content.");
      }
      
      if(site1Status.getRepo().equals(site2Status.getRepo()) && site2Service.isTag(site2Status.getBranch()) && site1Service.isTag(site1Status.getBranch()) && !site1Status.getBranch().equals(site2Status.getBranch())) {
        branch = site1Service.getBranchName();
        revision = null;
      } else if(site1Status.getRepo().equals(site2Status.getRepo()) && site2Service.isTag(site2Status.getBranch()) && !site1Status.getBranch().equals(site2Status.getBranch())) {
        site2Service.switchBranch(site1Service.getBranchName());
        site2Service.resetToRev(site1Service.getCurrentRevision());
      } else {
        System.out.println("Cloning content from ["+site1+"] to ["+site2+"]");
        revision = cloneContent(site1Service.getBaseDirectory(), site2Service);
      }
      
      if(tagname != null) {
        System.out.println("Tagging content for ["+site2+"] to serve.");
        site2Service.tag(tagname, comment);
        revision = null;
        branch = tagname;
      }
      
      System.out.println("Sending update message to ["+site2+"]");
      sendUpdateMessage(branch, revision);
      
    } catch(Exception e) {
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
  
  public void sendUpdateMessage(String branch, String revision) throws Exception {
    HttpClient client = new DefaultHttpClient();
    
    HttpPost post = new HttpPost(site2 + UPDATE_ENDPOINT);
    List<NameValuePair> parameters = new ArrayList<NameValuePair>();
    
    if(branch != null) {
      parameters.add(new BasicNameValuePair("branch", branch));
    }
    
    if(revision != null) {
      parameters.add(new BasicNameValuePair("rev", revision));
    }
    
    parameters.add(new BasicNameValuePair("comment", "Cloned from ["+site1+"]: " + comment));
    
    post.setEntity(new UrlEncodedFormEntity(parameters,"UTF-8"));
    
    HttpResponse response = client.execute(post);
    
    if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      String responseString = EntityUtils.toString(response.getEntity());
      
      if(responseString == null || !responseString.trim().equals("OK")) {
        System.err.println("Update message to ["+site2+"] failed.");
      }
    }
  }

  private String cloneContent(String source, GitService service) throws Exception {
    return GitService.moveContentToBranch(source, service, service.getBranchName(), comment);
  }

  public static Status getSiteStatus(String site) throws Exception {
    HttpClient client = new DefaultHttpClient();
    
    HttpGet get = new HttpGet(site + StatusCommand.JERSEY_ENDPOINT);
    HttpResponse response = client.execute(get);
    HttpEntity entity = response.getEntity();
    if(entity.getContentType().getValue().equals("application/json")) { 
      String responseContent = EntityUtils.toString(entity);            
      return new Gson().fromJson(responseContent, new TypeToken<Status>() {}.getType());
    }
    return null;
  }
  
  private GitService cloneSiteRepo(Status status) throws Exception {
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
      } finally {
        if(git != null) {
          git.close();
          git = null;
        }
      }
    }
    return git;
  }
}
