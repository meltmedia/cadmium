package com.meltmedia.cadmium.cli;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.FileSystemManager;

@Parameters(commandDescription="Initializes a new sites war from an existing war.", separators="=")
public class InitializeWarCommand {

  @Parameter(names="--existingWar", description="Path to an existing cadmium war.", required=true)
  private String war;
  
  @Parameter(names="--repo", description="Uri to remote github repo.", required=true)
  private String repoUri;
  
  @Parameter(names={"--branch","--tag"}, description="Initial branch to serve content from")
  private String branch;
  
  @Parameter(description="\"new war name\"", required=true, arity=1)
  private List<String> newWarNames;
  
  public void execute() throws Exception {
    if(FileSystemManager.canRead(war)) {
      ZipFile inZip = null;
      ZipOutputStream outZip = null;
      try{
        inZip = new ZipFile(war);
        outZip = new ZipOutputStream(new FileOutputStream(newWarNames.get(0)));
        
        ZipEntry cadmiumPropertiesEntry = null;
        cadmiumPropertiesEntry = inZip.getEntry("WEB-INF/cadmium.properties");
        Properties cadmiumProps = new Properties();
        cadmiumProps.load(inZip.getInputStream(cadmiumPropertiesEntry));
        
        cadmiumProps.setProperty("com.meltmedia.cadmium.git.uri", repoUri);
        if(branch != null) {
          cadmiumProps.setProperty("com.meltmedia.cadmium.branch", branch);
        } else {
          cadmiumProps.setProperty("com.meltmedia.cadmium.branch", "master");
        }
        
        Enumeration<? extends ZipEntry> entries = inZip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if(!e.getName().equals(cadmiumPropertiesEntry.getName())) {
              outZip.putNextEntry(e);
              if (!e.isDirectory()) {
                  copy(inZip.getInputStream(e), outZip);
              }
              outZip.closeEntry();
            } else {
              ZipEntry newCadmiumEntry = new ZipEntry(cadmiumPropertiesEntry.getName());
              outZip.putNextEntry(newCadmiumEntry);
              cadmiumProps.store(outZip, "Initial git properties for " + newWarNames.get(0));
              outZip.closeEntry();
            }
        }
      } finally {
        try{
          if(inZip != null) {
            inZip.close();
          }
        } catch(Exception e) {
          System.err.println("ERROR: Failed to close "+war);
        }
        try{
          if(outZip != null) {
            outZip.close();
          }
        } catch(Exception e) {
          System.err.println("ERROR: Failed to close "+newWarNames.get(0));
        }
      }
    } else {
      System.err.println("ERROR: \""+war+"\" does not exist or cannot be read.");
      System.exit(1);
    }
  }
  
  private static final byte[] BUFFER = new byte[4096 * 1024];
  
  public static void copy(InputStream input, OutputStream output) throws IOException {
      int bytesRead;
      while ((bytesRead = input.read(BUFFER))!= -1) {
          output.write(BUFFER, 0, bytesRead);
      }
  }
}
