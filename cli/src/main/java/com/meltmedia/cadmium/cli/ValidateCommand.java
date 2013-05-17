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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.FileSystemManager;
import jodd.jerry.Jerry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Parses all html files in a directory with a Jerry Library. This does not make any changes, it just tells if the files are parseable with Jerry. 
 * 
 * @see <a href="http://jodd.org/doc/jerry/index.html">Jerry Library</a>
 * 
 * @author John McEntire
 *
 */
@Parameters(commandDescription = "Validates a cadmium static content project.", separators="=")
public class ValidateCommand implements CliCommand {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  @Parameter(names = "-q", description="Quites output for use in scripts.", required=false)
  private boolean quite = false;
  
  @Parameter(description="<path>", required=false)
  private List<String> paths;
  
  @Override
  public String getCommandName() {
    return "validate";
  }

  @Override
  public void execute() throws Exception {
    logger.debug("Validating...");
    List<File> htmlFiles = listHtmlFiles();
    if(!quite) {
      System.out.println("Validating "+htmlFiles.size()+" files.");
    }
    boolean failed = false;
    Jerry.JerryParser jerryParser = Jerry.jerry().enableHtmlMode();
    jerryParser.getDOMBuilder().setCaseSensitive(false);
    jerryParser.getDOMBuilder().setParseSpecialTagsAsCdata(true);
    jerryParser.getDOMBuilder().setSelfCloseVoidTags(false);
    jerryParser.getDOMBuilder().setConditionalCommentExpression(null);
    jerryParser.getDOMBuilder().setEnableConditionalComments(false);
    jerryParser.getDOMBuilder().setImpliedEndTags(false);
    jerryParser.getDOMBuilder().setIgnoreComments(true);
    jerryParser.getDOMBuilder().setCollectErrors(true);
    jerryParser.getDOMBuilder().setCalculatePosition(true);
    for(File file : htmlFiles) {
      try {
        if(!quite) {
          System.out.print("  "+file + ": "); 
        }
        jerryParser.parse(FileSystemManager.getFileContents(file.getAbsolutePath()));
        if(!quite) {
          System.out.println(" passed");
        }
      } catch(Exception e) {
        failed = true;
        if(!quite) {
          System.out.println(" failed");
        }
        logger.error("Failed to parse:" + file, e);
      }
    }
    if(failed) {
      System.exit(1);
    }
  }
  
  /**
   * @return A list of html files in the paths specified by the CLI.
   */
  private List<File> listHtmlFiles() {
    File currentDir = new File(paths == null || paths.size() == 0 ? "." : paths.get(0));
    List<File> theFiles = new ArrayList<File>();
    List<File> theDirs = new ArrayList<File>();
    addCurrentDirFiles(theFiles, theDirs, currentDir);
    for(int i = 0; i<theDirs.size(); i++) {
      currentDir = theDirs.get(i);
      addCurrentDirFiles(theFiles, theDirs, currentDir);
    }
    return theFiles;
  }
  
  /**
   * Adds all html files to the list passed in.
   * 
   * @param theFiles The list to populate with the current directory's html files.
   * @param theDirs The list to populate with the current directory's child directories.
   * @param currentDir The current directory.
   */
  private void addCurrentDirFiles(List<File> theFiles, List<File> theDirs, File currentDir) {
    File dirs[] = currentDir.listFiles(new FileFilter() {

      @Override
      public boolean accept(File file) {
        return file.isDirectory() && !file.getName().startsWith(".") && !file.getName().equals("META-INF");
      }
      
    });
    
    File htmlFiles[] = currentDir.listFiles(new FileFilter() {

      @Override
      public boolean accept(File file) {
        return !file.isDirectory() && file.isFile() && file.canRead() && !file.getName().startsWith(".") && (file.getName().endsWith(".html") || file.getName().endsWith(".htm"));
      }
      
    });
    
    if(dirs != null && dirs.length > 0) {
      theDirs.addAll(Arrays.asList(dirs));
    }
    if(htmlFiles != null && htmlFiles.length > 0) {
      theFiles.addAll(Arrays.asList(htmlFiles));
    }
  }
  
  

}
