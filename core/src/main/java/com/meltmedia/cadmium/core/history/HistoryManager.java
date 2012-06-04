package com.meltmedia.cadmium.core.history;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.meltmedia.cadmium.core.FileSystemManager;

@Singleton
public class HistoryManager {
  public static final String HISTORY_FILE_NAME = "history.json";
  private final Logger log = LoggerFactory.getLogger(getClass());
  private List<HistoryEntry> history = new ArrayList<HistoryEntry>();
  private String contentRoot;
  private ExecutorService pool;
  
  
  @Inject
  public HistoryManager(@Named("applicationContentRoot") String contentRoot) throws Exception {
    this.contentRoot = contentRoot;
    
    readHistoryFile();
    
    pool = Executors.newSingleThreadExecutor();
  }
  
  public void logEvent(boolean maint, String openId, String comment) {
    logEvent("", "", openId, "", comment, maint, false);
  }
  
  public void logEvent(String branch, String sha, String openId, String directory, String comment, boolean revertible) {
    logEvent(branch, sha, openId, directory, comment, false, revertible);
  }

  public void logEvent(String branch, String sha, String openId, String directory, String comment, boolean maint, boolean revertible) {
    HistoryEntry lastEntry = history.size() > 0 ? history.get(0) : null;
    HistoryEntry newEntry = new HistoryEntry();
    newEntry.setTimestamp(new Date());
    if(lastEntry != null) {
      newEntry.setIndex(lastEntry.getIndex()+1);
    }
    int index = 1;
    while(lastEntry != null && (lastEntry.isRevertible() != revertible)) {
      if(history.size() > index) {
        lastEntry = history.get(index);
        index++;
      } else {
        lastEntry = null;
        break;
      }
    }
    if(lastEntry != null && lastEntry.getTimestamp() != null){
      lastEntry.setTimeLive(newEntry.getTimestamp().getTime() - lastEntry.getTimestamp().getTime());
      log.debug("The last history event lived [{}ms]", lastEntry.getTimeLive());
    }
    newEntry.setBranch(branch);
    newEntry.setRevision(sha);
    newEntry.setOpenId(openId);
    newEntry.setServedDirectory(directory);
    newEntry.setComment(comment);
    newEntry.setRevertible(revertible);
    log.info("Logging new History Event: branch[{}], sha[{}], openId[{}], directory[{}], revertible[{}], maint[{}], comment[{}]", new Object[] {branch, sha, openId, directory, revertible, maint, comment});
    
    history.add(0, newEntry);
    
    pool.submit(historyWriter);
  }
  
  public List<HistoryEntry> getHistory(Integer limit, boolean filter) {
    List<HistoryEntry> filteredHistory = new ArrayList<HistoryEntry>();
    
    if(limit == null || limit <= 0) {
      limit = history.size()+1;
    }
    log.info("Limit={}, filter={}", limit, filter);
    for(HistoryEntry entry : history) {
      if(!filter || entry.isRevertible()) {
        filteredHistory.add(entry);
      }
      if(filteredHistory.size() >= limit) {
        break;
      }
    }
    
    log.info("History size {}, filtered history size {}", history.size(), filteredHistory.size());
    return filteredHistory;
  }
  
  private void readHistoryFile() throws Exception {
    if(contentRoot != null) {
      String path = FileSystemManager.getFileIfCanRead(contentRoot, HISTORY_FILE_NAME);
      
      if(path != null) {
        Gson gson = new Gson();
        JsonReader reader = null;
        try{
          reader = new JsonReader(new FileReader(path));
          reader.setLenient(true);
          List<HistoryEntry> entries = gson.fromJson(reader, new TypeToken<List<HistoryEntry>>(){}.getType());
          
          if(entries != null) {
            history.addAll(entries);
          }
        } finally {
          log.info("Read in {} history entries", history.size());
          if(reader != null) {
            reader.close();
          }
        }
      }
    }
  }
  
  private Runnable historyWriter = new Runnable() {
    public void run() {
      if(contentRoot != null) {
        File path = new File(contentRoot, HISTORY_FILE_NAME);
        
        if(path.canWrite() || !path.exists()) {
          FileWriter writer = null;
          try {
            writer = new FileWriter(path, false);
            Gson gson = new Gson();
            gson.toJson(history, new TypeToken<List<HistoryEntry>>(){}.getType(), writer);
          } catch (Exception e) {
            log.warn("Failed to write history", e);
          } finally {
            if(writer != null) {
              try{
                writer.close();
              } catch(Exception e) {
                log.error("Failed to close history file", e);
              }
            }
          }
        }
      }
    }
  };
  
  public List<HistoryEntry> getHistory() {
    return this.history;
  }
}
