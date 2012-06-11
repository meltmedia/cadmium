package com.meltmedia.cadmium.cli;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.history.HistoryEntry;

@Parameters(commandDescription="Displays history from a site and allows a selection of which item to revert to.", separators="=")
public class RevertCommand extends AbstractAuthorizedOnly implements CliCommand {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Parameter(names="-i", description="Turns on interactive mode. (Not to be used with -n option)", required=false)
  private boolean interactive = false;
  
  @Parameter(names="-n", description="Specifies which history item to revert to. (Not to be used with -i option)", required=false)
  private Long index;
  
  @Parameter(names="--site", description="The url to the site to revert.", required=true)
  private String site;
  
  @Parameter(description="comment", required=true)
  private List<String> commentLines;
  private String comment;
  
  public void execute() throws Exception {
    if(!interactive && index == null) {
      System.err.println("Please specify either -i or -n options.");
      System.exit(1);
    }
    
    for(String commentLine : commentLines) {
      if(comment == null) {
        comment = "";
      } else {
        comment += " ";
      }
      comment += commentLine;
    }
    
    List<HistoryEntry> history = HistoryCommand.getHistory(site, -1, false, token);
    HistoryEntry selectedEntry = null;
    if(interactive && history != null && history.size() > 0) {
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
      System.err.println("No history returned by ["+site+"]");
    } else {
      selectedEntry = processIndex(history);
    }
    if(selectedEntry != null) {
      if(interactive) {
        System.out.println("Switching content on ["+site+"]");
      }
      log.debug("Reverting to branch {}, revision {}, comment [{}]", new Object [] {selectedEntry.getBranch(), selectedEntry.getRevision(), selectedEntry.getComment()});
      CloneCommand.sendUpdateMessage(site, selectedEntry.getBranch(), selectedEntry.getRevision(), comment, token);
    } else {
      System.exit(1);
    }
    
  }

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
