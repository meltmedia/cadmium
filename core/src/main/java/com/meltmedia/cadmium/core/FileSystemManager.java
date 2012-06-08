package com.meltmedia.cadmium.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
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
        return childFile.getAbsoluteFile().getAbsolutePath();
      }
    }
    return null;
  }
  
  public static String getFileContents(String path) throws Exception {
    InputStream in = null;
    String content = null;
    try {
      in = new FileInputStream(path);
      content = getFileContents(in);
    } finally {
      if(in != null) {
        try {
          in.close();
        } catch(Exception e) {}
      }
    }
    return content;
  }
  
  public static String getFileContents(InputStream in) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    streamCopy(in, out);
    String contents = out.toString();
    if(contents != null) {
      contents.trim();
    }
    return contents;
  }
  
  public static void writeStringToFile(String path, String filename, String content) throws Exception {
    OutputStream out = null;
    try{
      if(!exists(path)) {
        new File(path).mkdirs();
      }
      out = new FileOutputStream(new File(path, filename), false);
      ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
      streamCopy(in, out);
    } finally {
      if(out != null) {
        try {
          out.close();
        } catch(Exception e){}
      }
    }
  }
  
  public static String getFileIfCanRead(String path, String file) {
    File fileObj = new File(path, file);
    if(fileObj.canRead()){
      return fileObj.getAbsoluteFile().getAbsolutePath();
    }
    return null;
  }
  
  public static String getFileIfCanWrite(String path, String file) {
    File fileObj = new File(path, file);
    if(fileObj.canWrite()){
      return fileObj.getAbsoluteFile().getAbsolutePath();
    }
    return null;
  }
  
  public static String[] getFilesInDirectory(String directory, final String ext) {
    File dir = new File(directory);
    if(dir.isDirectory()) {
      String files[] = null;
      if(ext != null && ext.length() > 0) {
        files = dir.list(new FilenameFilter(){

          @Override
          public boolean accept(File file, String name) {
            return name.endsWith("."+ext);
          }
          
        });
      } else {
        files = dir.list();
      }
      if(files != null) {
        return files;
      }
    }
    return new String[] {};
  }

  
  public static String[] getDirectoriesInDirectory(String directory, final String baseName) {
    File dir = new File(directory);
    if(dir.isDirectory()) {
      String files[] = null;
      files = dir.list(new FilenameFilter(){

        @Override
        public boolean accept(File file, String name) {
          if(baseName != null && baseName.length() > 0) {
            return file.isAbsolute() && name.startsWith(baseName);
          }
          return file.isDirectory();
        }
        
      });
      if(files != null) {
        return files;
      }
    }
    return new String[] {};
  }
  
  public static boolean exists(String path) {
    return new File(path).exists();
  }
  
  public static boolean isDirector(String path) {
    return new File(path).isDirectory();
  }
  
  public static String getParent(String path) {
    return new File(path).getParent();
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
  
  public static void copyAllContent(final String source, final String target, final boolean ignoreHidden) throws Exception {
    File sourceFile = new File(source);
    File targetFile = new File(target);
    if(!targetFile.exists()) {
      targetFile.mkdirs();
    }
    if(sourceFile.exists() && sourceFile.canRead() && sourceFile.isDirectory() 
        && targetFile.exists() && targetFile.canWrite() && targetFile.isDirectory()) {
      List<File> copyList = new ArrayList<File>();
      FilenameFilter filter = new FilenameFilter() {

        @Override
        public boolean accept(File file, String name) {
          return !ignoreHidden || !name.startsWith(".");
        }
        
      };
      File files [] = sourceFile.listFiles(filter);
      if(files.length > 0) {
        copyList.addAll(Arrays.asList(files));
      }
      for(int index = 0; index < copyList.size(); index++) {
        File aFile = copyList.get(index);
        String relativePath = aFile.getAbsoluteFile().getAbsolutePath().replaceFirst(sourceFile.getAbsoluteFile().getAbsolutePath(), "");
        if(aFile.isDirectory()) {
          File newDir = new File(target, relativePath);
          if(newDir.mkdir()) {
            newDir.setExecutable(aFile.canExecute(), false);
            newDir.setReadable(aFile.canRead(), false);
            newDir.setWritable(aFile.canWrite(), false);
            newDir.setLastModified(aFile.lastModified());
            files = aFile.listFiles(filter);
            if(files.length > 0) {
              copyList.addAll(index + 1, Arrays.asList(files));
            }
          } else {
            log.warn("Failed to create new subdirectory \"{}\" in the target path \"{}\".", relativePath, target);
          }
        } else {
          File newFile = new File(target, relativePath);
          if(newFile.createNewFile()) {
            FileInputStream inStream = null;
            FileOutputStream outStream = null;
            try{
              inStream = new FileInputStream(aFile);
              outStream = new FileOutputStream(newFile);
              streamCopy(inStream, outStream);
            } finally {
              if(inStream != null) {
                try{
                  inStream.close();
                } catch(Exception e){}
              }
              if(outStream != null) {
                try{
                  outStream.flush();
                } catch(Exception e){}
                try{
                  outStream.close();
                } catch(Exception e){}
              }
            }
            newFile.setExecutable(aFile.canExecute(), false);
            newFile.setReadable(aFile.canRead(), false);
            newFile.setWritable(aFile.canWrite(), false);
            newFile.setLastModified(aFile.lastModified());
          }
        }
      }
    }
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
  
  public static void streamCopy(InputStream streamIn, OutputStream streamOut) throws IOException {
    streamCopy(streamIn, streamOut, false);
  }
    
  public static void streamCopy(InputStream streamIn, OutputStream streamOut, boolean leaveOutputOpen) throws IOException {
    ReadableByteChannel input = Channels.newChannel(streamIn);
    WritableByteChannel output = Channels.newChannel(streamOut);
    
    ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
    
    while (input.read(buffer) != -1) {
      buffer.flip();

      output.write(buffer);

      buffer.compact();
    }   
    
    buffer.flip();

    // Make sure the buffer is empty
    while (buffer.hasRemaining()) {
      output.write(buffer);
    }   

    input.close();
    if(!leaveOutputOpen) {
      output.close();
    }
  }
}
