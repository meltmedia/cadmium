package com.meltmedia.cadmium.cli;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.git.GitService;

@Parameters(commandDescription = "Initializes the content directory for a new war", separators="=")
public class InitializeCommand {
  
  @Parameter(names="--root", description="Shared content root", required=true)
  private String root;

  @Parameter(description="\"path to war\"", required=true)
  private List<String> wars;
  
  public void execute() throws Exception {
    setupSsh();
    
    for(String war : wars) {
      System.out.println("Initializing content for "+war);
      ZipFile zip = null;
      GitService service = null;
      try {
        zip = new ZipFile(war);
        File zipFile = new File(war);
        String warName = zipFile.getName();
        
        Properties cadmiumProps = new Properties();
        ZipEntry cad = zip.getEntry("WEB-INF/cadmium.properties");
        cadmiumProps.load(zip.getInputStream(cad));
        
        String branch = cadmiumProps.getProperty("com.meltmedia.cadmium.branch");
        String repo = cadmiumProps.getProperty("com.meltmedia.cadmium.git.uri");
        
        if(branch != null && repo != null) {
          service = GitService.initializeContentDirectory(repo, branch, root, warName);
        }
      } catch (Exception e) {
        System.err.println("Failed to initialize content for "+war);
      } finally {
        if(zip != null) {
          try{
            zip.close();
          } catch(Exception e){}
        }
        if(service != null) {
          try{
            service.close();
          } catch(Exception e){}
        }
      }
    }
  }

  private void setupSsh() {
    File userSsh = new File(System.getProperty("user.home"), ".ssh");
    if(userSsh.exists()) {
      GitService.setupSsh(userSsh.getAbsolutePath());
    }
  }
}
