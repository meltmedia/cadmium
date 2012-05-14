package com.meltmedia.cadmium.core;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileSystemManager {
  private static final Pattern FNAME_PATTERN = Pattern.compile("^(.+)_(\\d+)$", Pattern.CASE_INSENSITIVE);
  private static final Logger log = LoggerFactory.getLogger(FileSystemManager.class);
  
  public static String getChildDirectoryIfExists(String parent, String child) {
    File parentFile = new File(parent);
    if(parentFile.exists() && parentFile.isDirectory()) {
      File childFile = new File(parentFile, child);
      if(childFile.exists()) {
        return childFile.getAbsolutePath();
      }
    }
    return null;
  }
  
  public static boolean exists(String path) {
    return new File(path).exists();
  }
  
  public static boolean isDirector(String path) {
    return new File(path).isDirectory();
  }
  
  public static boolean canRead(String path) {
    return new File(path).canRead();
  }
  
  public static boolean canWrite(String path) {
    return new File(path).canWrite();
  }
  
  public static void deleteDeep(String path) {
    File pathFile = new File(path);
    if(pathFile.exists()) {
      if(pathFile.isDirectory()) {
        List<File> dirChildren = new ArrayList<File>();
        dirChildren.addAll(Arrays.asList(pathFile.listFiles()));
        if(!dirChildren.isEmpty()) {
          for(int i=0; i<dirChildren.size() ; i++) {
            File file = dirChildren.get(i);
            if(file.isDirectory()) {
              dirChildren.addAll(i+1, Arrays.asList(file.listFiles()));
            } else {
              file.delete();
            }
          }
          for(int i = dirChildren.size()-1; i >= 0; i--) {
            File file = dirChildren.get(i);
            if(file.isDirectory()) {
              file.delete();
            }
          }
        }
      }
      pathFile.delete();
    }
  }
  
  public static String getNextDirInSequence(String lastDirName) {
    File lastDir = new File(lastDirName);
    String newDir = lastDirName;
    if(lastDir.exists()) {
      File parentDir = lastDir.getParentFile();
      String dirName = lastDir.getName();
      if(parentDir.canWrite()) {
        int nextNum = 0;
        Matcher fnameMatcher = FNAME_PATTERN.matcher(dirName);
        if(fnameMatcher.matches()) {
          nextNum = Integer.parseInt(fnameMatcher.group(2));
          dirName = fnameMatcher.group(1);
        }
        nextNum++;
        dirName += "_" + nextNum;
                              
        File newDirFile = new File(parentDir, dirName);
        if(newDirFile.exists()) {
          deleteDeep(newDirFile.getAbsolutePath());
        }
        newDir = newDirFile.getAbsolutePath();
      } else {
        newDir = null;
      }
    }
    return newDir;
  }
  
  public static void cleanUpOld(final String lastUpdatedDir, final int numToKeep) {
    final File lastUpdated = new File(lastUpdatedDir);
    if(lastUpdated.exists()) {
      File parentDir = lastUpdated.getParentFile();
      Matcher nameMatcher = FNAME_PATTERN.matcher(lastUpdated.getName());
      if(nameMatcher.matches()){
        final String prefixName = nameMatcher.group(1);
        final int dirNumber = Integer.parseInt(nameMatcher.group(2));
        File renderedDirs[] = parentDir.listFiles(new FilenameFilter() {

          @Override
          public boolean accept(File file, String name) {
            if(name.startsWith(prefixName)) {
              return true;
            }
            return false;
          }
          
        });
        
        for(File renderedDir : renderedDirs) {
          if(renderedDir.getName().equals(prefixName) && dirNumber > numToKeep) {
            try{
              deleteDeep(renderedDir.getAbsolutePath());
            } catch(Exception e) {
              log.warn("Failed to delete old dir {}, {}", renderedDir.getName(), e.getMessage());
            }
          } else {
            Matcher otherNameMatcher = FNAME_PATTERN.matcher(renderedDir.getName());
            if(otherNameMatcher.matches()) {
              int otherDirNumber = Integer.parseInt(otherNameMatcher.group(2));
              if(otherDirNumber < (dirNumber - numToKeep)) {
                try{
                  deleteDeep(renderedDir.getAbsolutePath());
                } catch(Exception e) {
                  log.warn("Failed to delete old dir {}, {}", renderedDir.getName(), e.getMessage());
                }
              }
            }
          }
        }
      }
    }
  }
}
