package com.meltmedia.cadmium.jetty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Runs Cadmium site inside of a jetty container.
 */
public final class Launcher {
  private static File workDir = new File(System.getProperty("work.directory", System.getProperty("java.io.tmpdir")));

  public static void log(String msg) {
    System.out.println(msg);
  }

  public static void main(String args[]) throws Exception {
    ProtectionDomain domain = Launcher.class.getProtectionDomain();
    URL warLocation = domain.getCodeSource().getLocation();
    String warExtLocation = warLocation.toExternalForm();
    ClassLoader cl = updateClasspath(warLocation.getPath());
    new AppServer(workDir).runServer(warExtLocation);
  }

  public static ClassLoader updateClasspath(String war) throws Exception {
    ZipFile warFile = new ZipFile(war);
    if(!workDir.exists()) {
      workDir.mkdirs();
    }

    File serverLibDir = new File(workDir.getParentFile(), "lib");

    File warExtDir = new File(workDir, warFile.getName().substring(warFile.getName().lastIndexOf("/")));
    if(warExtDir.exists()) {
      clearDir(warExtDir);
    }
    warExtDir.mkdirs();
    URLClassLoader cl = (URLClassLoader)ClassLoader.getSystemClassLoader();
    Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
    boolean isAccessible = addURL.isAccessible();
    addURL.setAccessible(true);

    try {

      if (serverLibDir.exists()) {
        File libFiles[] = serverLibDir.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File file, String s) {
            return s.endsWith(".jar");
          }
        });
        for (File libFile : libFiles) {
          try {
            URL libFileURL = libFile.toURI().toURL();
            addURL.invoke(cl, new Object[]{libFileURL});
          } catch (Exception e) {
            log("Failed to add [" + libFile + "] to classpath.");
          }
        }
      }

      List<URL> cp = new ArrayList<URL>();

      Enumeration<ZipEntry> en = (Enumeration<ZipEntry>) warFile.entries();
      while(en.hasMoreElements()) {
        ZipEntry entry = en.nextElement();
        if(!entry.isDirectory()) {
          if (entry.getName().startsWith("WEB-INF/lib/") && entry.getName().endsWith(".jar")) {
            File libFile = new File(warExtDir, entry.getName().substring(entry.getName().lastIndexOf('/')));
            try {
              URL libFileURL = libFile.toURI().toURL();
              extractFile(libFile, warFile.getInputStream(entry));
              addURL.invoke(cl, new Object[] {libFileURL});
            } catch(Throwable t) {
              log("Failed to add ["+libFile+"] to classpath");
            }
          }
        }
      }
      addURL.setAccessible(isAccessible);
      return cl;
    } finally {
      warFile.close();
    }
  }

  public static void clearDir(File dir) throws Exception {
    File dirContents[] = dir.listFiles();
    for(File dirFile : dirContents) {
      if(dirFile.isDirectory()) {
        clearDir(dirFile);
      } else {
        dirFile.delete();
      }
    }
    dir.delete();
  }

  public static void extractFile(File toFile, InputStream in) throws Exception {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(toFile);
      FileChannel destination = out.getChannel();
      ReadableByteChannel source = Channels.newChannel(in);
      destination.transferFrom(source, 0, Integer.MAX_VALUE);
    } finally {
      try {
        in.close();
      } catch(Throwable t){}
      try {
        out.close();
      } catch(Throwable t){}
    }
  }
}
