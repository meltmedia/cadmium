package com.meltmedia.cadmium.search;

import static jodd.lagarto.dom.jerry.Jerry.jerry;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import jodd.lagarto.dom.jerry.Jerry;

import com.google.inject.Inject;
import com.meltmedia.cadmium.core.meta.ConfigProcessor;

public class SearchContentPreprocessor  implements ConfigProcessor, IndexSearcherProvider {
  
  public static FileFilter HTML_FILE_FILTER = new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return pathname.isFile() && pathname.getPath().matches("\\.htm[l]?\\Z");
    }
  };
  
  public static FileFilter DIR_FILTER = new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return pathname.isDirectory();
    }
  };
  
  public static FileFilter NOT_INF_DIR_FILTER = new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return pathname.isDirectory() && pathname.getName().endsWith("-INF");
    }
  };
  
  public static Comparator<File> FILE_NAME_COMPARATOR = new Comparator<File>() {
    @Override
    public int compare(File file1, File file2) {
      return file1.getName().compareTo(file2.getName());
    }
  };
  
  /**
   * A template class that scans the content directory, starting at the root, and
   * calls scan(File) for every file that matches the provided content filter.
   * 
   * @author Christian Trimble
   */
  public static abstract class ContentScanTemplate
  {
    private FileFilter contentFilter;

    public ContentScanTemplate(FileFilter contentFilter) {
      this.contentFilter = contentFilter;
    }
    
    public void scan( File contentRoot ) throws Exception {
      // create the frontier and add the content root.
      LinkedList<File> frontier = new LinkedList<File>();

      // scan the content root dir for html files.
      for( File htmlFile : contentRoot.listFiles(contentFilter)) {
        handleFile(htmlFile);
      }
      
      // add the non "-INF" directories, in a predictable order.
      frontier.subList(0, 0).addAll(Arrays.asList(sort(contentRoot.listFiles(NOT_INF_DIR_FILTER), FILE_NAME_COMPARATOR)));
      
      while( !frontier.isEmpty() ) {
        File dir = frontier.removeFirst();
        
        // scan the html files in the directory.
        for( File htmlFile : dir.listFiles(contentFilter)) {
          handleFile(htmlFile);
        }
   
        // add the directories, in a predictable order.
        frontier.subList(0, 0).addAll(Arrays.asList(sort(dir.listFiles(DIR_FILTER), FILE_NAME_COMPARATOR)));
      }
    }
    
    /**
     * An call to Arrays.sort(array, comparator) that returns the array argument after the sort.
     * 
     * @param array the array to sort.
     * @param comparator the comparator to sort with.
     * @return the array argument.
     */
    private static <T> T[] sort( T[] array, Comparator<T> comparator ) {
      Arrays.sort(array, comparator);
      return array;
    }
    
    public abstract void handleFile( File file )
      throws Exception;
  }
  
  private File indexDir;
  private File dataDir;
  private Directory liveDirectory = null;
  private IndexReader liveIndexReader = null;
  private Directory stagedDirectory = null;
  private IndexReader stagedIndexReader = null;
  private static Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
  

  @Override
  public synchronized void processFromDirectory(String metaDir) throws Exception {
    IOUtils.closeQuietly(stagedDirectory);
    IOUtils.closeQuietly(stagedIndexReader);
    stagedDirectory = new NIOFSDirectory(indexDir);
    IndexWriter iwriter = null;
    try {
      iwriter = new IndexWriter(stagedDirectory, new IndexWriterConfig(Version.LUCENE_36, analyzer).setRAMBufferSizeMB(5));
      writeIndex(iwriter, new File(metaDir).getParentFile());
    }
    finally {
      IOUtils.closeQuietly(iwriter);
    }
    stagedIndexReader = IndexReader.open(stagedDirectory);
  }
  
  void writeIndex( final IndexWriter indexWriter, File contentDir ) throws Exception {
    new ContentScanTemplate(HTML_FILE_FILTER) {
      @Override
      public void handleFile(File file) throws Exception {
        Jerry jerry = new Jerry.JerryParser().enableHtmlMode().parse(FileUtils.readFileToString(file, "UTF-8"));
        String title = jerry.$("/html/head/title").text();
        String textContent = jerry.$("/html/body").text();
        
        Document doc = new Document();
        doc.add(new Field("title", title, Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("content", textContent, Field.Store.YES, Field.Index.ANALYZED));
        indexWriter.addDocument(doc);
      }
    }.scan(contentDir); 
    
  }

  @Override
  public synchronized void makeLive() {
    //  TODO: start write lock.
    if( stagedDirectory != null && stagedIndexReader != null) {
      IOUtils.closeQuietly(liveDirectory);
      IOUtils.closeQuietly(liveIndexReader);
      liveDirectory = stagedDirectory;
      liveIndexReader = stagedIndexReader;
      stagedDirectory = null;
      stagedIndexReader = null;
    }
    //  TODO: end write lock.
  }
  
  public void finalize() {
    IOUtils.closeQuietly(stagedDirectory);
    IOUtils.closeQuietly(stagedIndexReader);
    IOUtils.closeQuietly(liveIndexReader);
    IOUtils.closeQuietly(liveDirectory);
 }

  @Override
  public IndexSearcher startSearch() {
    //  TODO: start read on read/write lock.
    //  TODO: return the current searcher.
    return null;
  }

  @Override
  public void endSearch() {
    // TODO: release read lock.    
  }

  @Override
  public Analyzer getAnalyzer() {
    // TODO Return the analyzer
    return null;
  }

}
