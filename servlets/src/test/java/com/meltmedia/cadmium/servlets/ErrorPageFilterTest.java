package com.meltmedia.cadmium.servlets;

import java.io.File;
import java.io.FileWriter;

import org.junit.Test;
import org.junit.Before;

import com.meltmedia.cadmium.core.FileSystemManager;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ErrorPageFilterTest {
  
  @Before
  public void setup() throws Exception {
    File targetDir = new File("./target/error_pages");
    if(!targetDir.exists()) {
      targetDir.mkdirs();
    }
        
    
    File newFile = new File("./target/error_pages/404.html");
    newFile.createNewFile();
    
    newFile = new File("./target/error_pages/410.html");
    newFile.createNewFile();
    
    newFile = new File("./target/error_pages/40x.html");
    newFile.createNewFile();
    
    newFile = new File("./target/error_pages/4xx.html");
    newFile.createNewFile();
    
    newFile = new File("./target/error_pages/510.html");
    newFile.createNewFile();
    
    newFile = new File("./target/error_pages/503.html");
    newFile.createNewFile();
    
    newFile = new File("./target/error_pages/50x.html");
    newFile.createNewFile();
    
    newFile = new File("./target/error_pages/5xx.html");
    newFile.createNewFile();
    
       
    
    FileWriter writer = null;
    try{
      
      writer = new FileWriter(newFile);
      writer.write("content");
    } 
    finally {
      
      writer.close();
    }    
    
  }
  
  @Test
  public void testFileExists() {
   
    String[] fileNames = {"404.html", "40x.html", "4xx.html", "510.html", "50x.html", "5xx.html"};
    boolean fileFound = false;
    
    ErrorPageFilter filter = new ErrorPageFilter();
    
    for(int i=0; i<fileNames.length; i++) {
      
      fileFound = filter.fileExists("target/error_pages/", fileNames[i]);
      if(fileFound) {
        
        System.out.println(fileNames[i] + " exists");
        System.out.println("");
      }
      else {
        
        System.out.println(fileNames[i] + " does NOT exist");
        System.out.println("");
      }
    }     
    
  }
  
  @Test
  public void testGetErrorPage() {
    int[] codes = {200, 301, 401, 404, 467, 410, 501, 503, 510, 567, 700, 302, 225};
    
    ErrorPageFilter filter = new ErrorPageFilter();
    
    for(int i=0; i<codes.length; i ++) {
      
      if(400 <= codes[i] && codes[i] < 600) {
        String fileName = filter.getErrorPage("target/error_pages/", codes[i]);
        
        System.out.println("");
        System.out.println("Error Code input: " + codes[i]);
        System.out.println("Error file name: " + fileName);
        System.out.println("");
      }
      
      else {
        System.out.println("Serving normal or default content for code: " + codes[i]);
      }
    }
  }     
  
}
