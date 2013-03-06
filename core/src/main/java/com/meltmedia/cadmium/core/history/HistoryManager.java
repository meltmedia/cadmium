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
package com.meltmedia.cadmium.core.history;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.meltmedia.cadmium.core.ApplicationContentRoot;
import com.meltmedia.cadmium.core.FileSystemManager;
import com.meltmedia.cadmium.core.commands.GitLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Singleton
public class HistoryManager {
  public static final String HISTORY_FILE_NAME = "history.json";
  private final Logger log = LoggerFactory.getLogger(getClass());
  private List<HistoryEntry> history = new ArrayList<HistoryEntry>();
  private String contentRoot;
  
  @Inject
  private ExecutorService pool;
  
  
  @Inject
  public HistoryManager(@ApplicationContentRoot String contentRoot) throws Exception {
    this.contentRoot = contentRoot;
    
    readHistoryFile();
  }
  
  public HistoryManager(String contentRoot, ExecutorService pool) throws Exception {
    this.contentRoot = contentRoot;
    this.pool = pool;
    readHistoryFile();
  }
  
  public void logEvent(boolean maint, String openId, String comment) {
    logEvent(HistoryEntry.EntryType.MAINT, null, openId, "", "", comment, maint, false, false, true);
  }
  
  public void logEvent(HistoryEntry.EntryType type, GitLocation gitPointer, String openId, String directory, String uuid, String comment, boolean revertible, boolean finished) {
    logEvent(type, gitPointer, openId, directory, uuid, comment, false, revertible, false, finished);
  }

  public void logEvent(HistoryEntry.EntryType type, GitLocation gitPointer, String openId, String directory, String uuid, String comment, boolean maint, boolean revertible, boolean failed, boolean finished) {
    HistoryEntry lastEntry = history.size() > 0 ? history.get(0) : null;
    if(uuid != null && lastEntry != null && lastEntry.getUuid() != null && uuid.trim().length() > 0 && uuid.equals(lastEntry.getUuid())) {
      log.debug("Last history entry was a dup.");
      return;
    }
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
    // the git pointer was not added to the HistoryEntry yet, to avoid changes to the message format.
    String repository = gitPointer != null && gitPointer.getRepository() != null ? gitPointer.getRepository() : "";
    String branch = gitPointer != null && gitPointer.getBranch() != null ? gitPointer.getBranch() : "";
    String revision = gitPointer != null && gitPointer.getRevision() != null ? gitPointer.getRevision() : "";

    newEntry.setType(type);
    newEntry.setRepoUrl(repository);
    newEntry.setBranch(branch);
    newEntry.setRevision(revision);
    newEntry.setOpenId(openId);
    newEntry.setServedDirectory(directory);
    newEntry.setUuid(uuid);
    newEntry.setComment(comment);
    newEntry.setMaintenance(maint);
    newEntry.setRevertible(revertible);
    newEntry.setFailed(failed);
    newEntry.setFinished(finished);
    log.info("Logging new {} History Event: repoUrl[{}], branch[{}], sha[{}], openId[{}], directory[{}], uuid[{}], revertible[{}], maint[{}], failed[{}], comment[{}]", new Object[] {type, repository, branch, revision, openId, directory, uuid, revertible, maint, failed, comment});
    
    history.add(0, newEntry);
    
    pool.execute(historyWriter);
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
  
  public void markHistoryEntryAsFinished(String uuid) {
    for(HistoryEntry entry : history) {
      if(!entry.isFinished() && entry.getUuid() != null && entry.getUuid().equals(uuid) && entry.isRevertible()) {
        entry.setFinished(true);
        log.debug("Marked {} as finished.", entry);
        pool.execute(historyWriter);
        break;
      }
    }
  }
  
  public HistoryEntry getLatestHistoryEntryByUUID(String uuid, Date since) {
    for(HistoryEntry entry : history) {
      if((since == null || entry.getTimestamp().after(since)) && entry.getUuid() != null && entry.getUuid().equals(uuid)) {
        log.debug("Got latest entry for {}, {}", uuid, entry);
        return entry;
      }
    }
    return null;
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
