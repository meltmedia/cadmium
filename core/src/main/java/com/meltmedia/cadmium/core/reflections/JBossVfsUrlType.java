package com.meltmedia.cadmium.core.reflections;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.plugins.vfs.VirtualFileURLConnection;
import org.reflections.vfs.Vfs.Dir;
import org.reflections.vfs.Vfs.File;
import org.reflections.vfs.Vfs.UrlType;

/**
 * JBoss classpath scanning code from XChains:
 * https://github.com/ctrimble/xchain/blob/master/core/src/main/java/org/xchain/framework/scanner/VfsProtocolScanner.java
 * 
 * @author Christian Trimble
 *
 */

public class JBossVfsUrlType implements UrlType {
  
  public static final List<String> VFS_PROTOCOLS = Arrays.asList(new String[] { "vfszip", "vfsfile", "vfsmemory" });

  @Override
  public Dir createDir(URL vfsUrl) throws Exception {
    return new VfsFileDir(vfsUrl);
  }

  @Override
  public boolean matches(URL url) throws Exception {
    return VFS_PROTOCOLS.contains(url.getProtocol());
  }
  
  public static class VfsFileDir
    implements Dir
  {
    VirtualFileURLConnection vfConn;
    VirtualFile vFile;

    public VfsFileDir( URL vfsUrl ) throws IOException {
      vfConn = (VirtualFileURLConnection)vfsUrl.openConnection();
      vFile = vfConn.getContent();
    }

    @Override
    public void close() {
      // The working code from XChains does not call close on the vFile object.
    }

    // this would be better with the built in recursive list and an on the fly type converting iterator.
    @Override
    public Iterable<File> getFiles() {
      return new Iterable<File>() {
        @Override
        public Iterator<File> iterator() {
          final LinkedList<VirtualFile> frontier = new LinkedList<VirtualFile>();
          frontier.add(vFile);
          return new Iterator<File>() {
            @Override
            public boolean hasNext() {
              try {
                while(!frontier.isEmpty() && !frontier.getFirst().isLeaf())
                  frontier.addAll(frontier.removeFirst().getChildren());
                return !frontier.isEmpty();
              }
              catch( Exception e ) {
                return false;
              }
            }

            @Override
            public File next() {
              try {
                while(!frontier.isEmpty() && !frontier.getFirst().isLeaf())
                  frontier.addAll(frontier.removeFirst().getChildren());
 
                if( frontier.isEmpty() ) throw new NoSuchElementException();
                
                final VirtualFile leaf = frontier.removeFirst();
                
                return new File() {
                  @Override
                  public String getName() {
                    return leaf.getName();
                  }

                  @Override
                  public String getRelativePath() {
                    return leaf.getPathName().substring(vFile.getPathName().length()+1);
                  }

                  @Override
                  public InputStream openInputStream() throws IOException {
                    return leaf.openStream();
                  }
                  
                };
              
              }
              catch( Exception e ) {
                throw new NoSuchElementException();
              }              

            }

            @Override
            public void remove() {
              throw new UnsupportedOperationException("Remove not supported by the JBoss VFS Scanner.");
            }
          };
        }
        
      };
    }

    @Override
    public String getPath() {
      return vFile.getPathName();
    }
    
  }
}
