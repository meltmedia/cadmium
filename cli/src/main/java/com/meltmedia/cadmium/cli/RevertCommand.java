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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.history.HistoryEntry;

/**
 * Displays the history of a Cadmium site and allows a selection of a revertable history entry to revert the site to, in normal mode.  
 * If a in <code>-n</code> option is passed, then the history is not displayed and a update is issued to the Cadmium site with the 
 * information from the given history index.
 * 
 * @author John McEntire
 * @author Brian Barr
 *
 */
@Parameters(commandDescription="Displays history from a site and allows a selection of which item to revert to.", separators="=")
public class RevertCommand extends AbstractAuthorizedOnly implements CliCommand {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Parameter(names="-n", description="Specifies which history item to revert to.", required=false)
  private Long index;
  
  @Parameter(description="<site>", required=true)
  private List<String> site;
  
  @Parameter(names={"--message", "-m"}, description="comment", required=true)  
  private String comment;
  
  public void execute() throws Exception {    
    boolean interactive = index == null;
    
    String siteUrl = getSecureBaseUrl(site.get(0));
    
    List<HistoryEntry> history = HistoryCommand.getHistory(siteUrl, -1, false, token);
    HistoryEntry selectedEntry = null;
    if(index == null && history != null && history.size() > 0) {
      HistoryCommand.displayHistory(history, true, 5);
      
      String response = null;
      while(index == null || "exit".equals(response)) {
        response = System.console().readLine("Enter history index: ");
        if(response != null && !"exit".equals(response) && response.matches("^[-]?\\d+$")){
          index = Long.parseLong(response);
          selectedEntry = processIndex(history);
        } else if(response != null && !"exit".equals(response)) {
          System.err.println("Invalid entry!");
        }
      }
    } else if(history == null || history.isEmpty()) {
      System.err.println("No history returned by ["+siteUrl+"]");
    } else {
      selectedEntry = processIndex(history);
    }
    if(selectedEntry != null) {
      if(interactive) {
        System.out.println("Switching content on ["+siteUrl+"]");
      }
      String endpoint = UpdateCommand.UPDATE_ENDPOINT;
      if(selectedEntry.getType() == HistoryEntry.EntryType.CONFIG) {
        endpoint = UpdateConfigCommand.UPDATE_CONFIG_ENDPOINT;
      } else if(selectedEntry.getType() == HistoryEntry.EntryType.MAINT) {
        log.error("Invalid history entry type: {}", selectedEntry.getType());
        System.err.println("Invalid history entry type: "+selectedEntry.getType());
        System.exit(1);
      }
      log.debug("Reverting {} to repo {}, branch {}, revision {}, comment [{}]", new Object [] {endpoint, selectedEntry.getRepoUrl(), selectedEntry.getBranch(), selectedEntry.getRevision(), selectedEntry.getComment()});
      UpdateCommand.sendUpdateMessage(siteUrl, selectedEntry.getRepoUrl(), selectedEntry.getBranch(), selectedEntry.getRevision(), comment, token, endpoint);
    } else {
      System.exit(1);
    }
    
  }

  /**
   * Prompts for and returns a selected history entry or if the <code>-n</code> option is passed that history entry is returned.
   * 
   * @param history
   * @return
   */
  private HistoryEntry processIndex(List<HistoryEntry> history) {
    log.debug("I received entry: "+index);
    HistoryEntry selectedEntry = null;
    if(index != null && index > 0) {
      for(HistoryEntry entry : history) {
        if(entry.getIndex() == index) {
          if(!entry.isRevertible()) {
            System.err.println("Selected index ["+index+"] is not revertible.");
            index = null;
          } else {
            selectedEntry = entry;
          }
          break;
        }
      }
      if(index == null) {
        selectedEntry = null;
      } else if(selectedEntry == null) {
        System.err.println("Invalid entry!");
        index = null;
      }
    } else if(index != null && index < 0) {
      for(HistoryEntry entry : history) {
        if(entry.isRevertible()) {
          if(index++ == 0) {
            selectedEntry = entry;
            index = entry.getIndex();
            break;
          }
        }
      }
      if(selectedEntry == null) {
        System.err.println("Invalid entry!");
        index = null;
      }
    } else {
      index = null;
      System.err.println("Invalid entry!");
    }
    return selectedEntry;
  }

  @Override
  public String getCommandName() {
    return "revert";
  }
}
