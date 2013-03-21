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
package com.meltmedia.cadmium.blackbox.test;

import junit.framework.AssertionFailedError;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.jgit.util.StringUtils;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Assertions for use in JUnit tests to simplify testing of local VS. remote content.</p>
 * 
 * @author John McEntire
 *
 */
public final class CadmiumAssertions {
  
  /**
   * <p>Asserts the a file exists locally.</p>
   * @param message The message that will be in the error if the file doesn't exist.
   * @param toCheck The file to check.
   */
  public static void assertExistsLocally(String message, File toCheck) {
    if(toCheck == null || !toCheck.exists()) {
      throw new AssertionFailedError(message);
    }
  }
  
  /**
   * <p>Asserts that a remote resource exists.</p>
   * @param message The message that will be in the error if the remote resource doesn't exist.
   * @param remoteLocation A string containing the URL location of the remote resource to check.
   * @param username The Basic HTTP auth username.
   * @param password The Basic HTTP auth password.
   * @return The content of the remote file.
   */
  public static HttpEntity assertExistsRemotely(String message, String remoteLocation, String username, String password) {
    DefaultHttpClient client = new DefaultHttpClient();
    try {
      HttpGet get = new HttpGet(remoteLocation);
      if(!StringUtils.isEmptyOrNull(username) && !StringUtils.isEmptyOrNull(password)) {
        get.setHeader("Authorization", encodeForBasicAuth(username, password));
      }
     
      HttpResponse resp = client.execute(get);
      if(resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new AssertionFailedError(message);
      }
      return resp.getEntity();
    } catch (ClientProtocolException e) {
      throw new AssertionFailedError(e.getMessage() + ": " + message);
    } catch (IOException e) {
      throw new AssertionFailedError(e.getMessage() + ": " + message);
    } 
  }

  /**
   * <p>Asserts that a remote resource exists.</p>
   * @param message The message that will be in the error if the remote resource doesn't exist.
   * @param remoteLocation A string containing the URL location of the remote resource to check.
   * @return The content of the remote file.
   */
  public static HttpEntity assertExistsRemotely(String message, String remoteLocation) {
    return assertExistsRemotely(message, remoteLocation, null, null);
  }
  
  /**
   * <p>Asserts that the local and remote files pass to this method exist and are identical.</p>
   * @param message The message that will be in the error if the local file isn't the same as the remote file.
   * @param toCheck The file to check
   * @param username The Basic HTTP auth username.
   * @param password The Basic HTTP auth password.
   * @param remoteLocation A string containing the URL location of the remote resource to check.
   */
  public static void assertResourcesEqual(String message, File toCheck, String remoteLocation, String username, String password) {
    FileReader reader = null;
    assertExistsLocally(message, toCheck);
    try {
      reader = new FileReader(toCheck);
      byte fileContents[] = IOUtils.toByteArray(reader);
      byte remoteContents[] = EntityUtils.toByteArray(assertExistsRemotely(message, remoteLocation, username, password));
      if(!Arrays.equals(fileContents, remoteContents)){
        throw new AssertionFailedError(message);
      }
    } catch (IOException e) {
      throw new AssertionFailedError(e.getMessage() + ": " + message);
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  /**
   * <p>Asserts that the local and remote files pass to this method exist and are identical.</p>
   * @param message The message that will be in the error if the local file isn't the same as the remote file.
   * @param toCheck The file to check
   * @param remoteLocation A string containing the URL location of the remote resource to check.
   */
  public static void assertResourcesEqual(String message, File toCheck, String remoteLocation) {
    assertResourcesEqual(message, toCheck, remoteLocation, null, null);
  }

  /**
   * <p>Asserts that the local directory contents have all been deployed to the remote URL prefix.</p>
   * @param message The message that will be in the error if the local directory hasn't been deployed to the remote URL prefix.
   * @param directory The directory locally that should have been deployed.
   * @param urlPrefix The URL prefix that should have the contents of the directory.
   * @param username The Basic HTTP auth username.
   * @param password The Basic HTTP auth password.
   */
  public static void assertContentDeployed(String message, File directory, String urlPrefix, String username, String password) {
    if(directory == null || !directory.isDirectory() || urlPrefix == null) {
      throw new AssertionFailedError(message);
    }
    File files[] = getAllDirectoryContents(directory, "META-INF", ".git");
    String basePath = directory.getAbsoluteFile().getAbsolutePath();
    if(urlPrefix.endsWith("/")) {
      urlPrefix = urlPrefix.substring(0, urlPrefix.length() - 2);
    }
    for(File file : files) {
      if(file.isFile()) {
        String httpPath = urlPrefix + "/" + file.getAbsoluteFile().getAbsolutePath().replaceFirst(basePath + "/", "");
        assertResourcesEqual(message, file, httpPath, username, password);
      }
    }
  }

  /**
   * <p>Asserts that the local directory contents have all been deployed to the remote URL prefix.</p>
   * @param message The message that will be in the error if the local directory hasn't been deployed to the remote URL prefix.
   * @param directory The directory locally that should have been deployed.
   * @param urlPrefix The URL prefix that should have the contents of the directory.
   */
  public static void assertContentDeployed(String message, File directory, String urlPrefix){
    assertContentDeployed(message, directory, urlPrefix, null, null);
  }
  
  /**
   * <p>Gets an array of file objects representing the directory contents recursively listed. (<em>Not including directories named in the excludes.</em>)</p>
   * @param directory The directory to list.
   * @param excludes The directory and/or filenames to exclude.
   * @return The list of files.
   */
  private static File[] getAllDirectoryContents(File directory, String... excludes) throws IllegalArgumentException {
    if(directory == null || !directory.exists() || !directory.isDirectory()) {
      throw new IllegalArgumentException("Path \""+directory+"\" either does not exist or is not a directory.");
    }
    FilenameFilter filter = new FilenameExclusionFilter(excludes);
    List<File> files = new ArrayList<File>();
    File listedFiles[] = directory.listFiles(filter);
    if(listedFiles != null) {
      files.addAll(Arrays.asList(listedFiles));
    }
    for(int i = 0 ; i<files.size(); i++) {
      File aFile = files.get(i);
      if(aFile.isDirectory()) {
        listedFiles = aFile.listFiles(filter);
        if(listedFiles != null && listedFiles.length > 0){
          files.addAll(i+1, Arrays.asList(listedFiles));
        }
      }
    }
    
    return files.toArray(new File [] {});
  }
  
  /**
   * <p>The {@link FilenameFilter} implementation used by {@link CadmiumAssertions#getAllDirectoryContents(File, String...)}.</p>
   * @author John McEntire
   *
   */
  private static class FilenameExclusionFilter implements FilenameFilter {

    private List<String> exclusions = new ArrayList<String>();
    
    /**
     * <p>Constructor passed the array of string containing the filenames to exclude.</p>
     * @param exclusions The array to exclude.
     */
    FilenameExclusionFilter(String[] exclusions) {
      if(exclusions != null) {
        this.exclusions = Arrays.asList(exclusions);
      }
    }
    
    /**
     * See {@link FilenameFilter#accept(File, String)}
     */
    @Override
    public boolean accept(File file, String name) {
      return !exclusions.contains(name);
    }
    
  }

  private static String encodeForBasicAuth(String username, String password) {
    return "Basic "+ new String(new BASE64Encoder().encode((username+":"+password).getBytes()));
  }

}
