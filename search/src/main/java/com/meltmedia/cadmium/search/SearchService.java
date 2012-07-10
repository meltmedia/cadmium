package com.meltmedia.cadmium.search;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

@Path("/search")
public class SearchService {

  private IndexSearcher index;
  private Analyzer analyzer;
  private QueryParser parser;
  private Object syncObject = new Object();
  
  public void updateDirectory(String indexDirectory) throws IOException {
    synchronized(syncObject) {
      close(index);
      index = new IndexSearcher(IndexReader.open(new NIOFSDirectory(new File(indexDirectory))));
      analyzer = new StandardAnalyzer(Version.LUCENE_36);
      parser = new MultiFieldQueryParser(Version.LUCENE_36, new String[]{"title", "content"}, analyzer);
    }
  }
  
  @GET
  @Produces("application/json")
  public Map<String, Object> search(@QueryParam("terms") String query) throws Exception {
    synchronized(syncObject) {
      Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
      
      resultMap.put("number-hits", 0);
      List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
      resultMap.put("results", resultList);
      if(index != null && parser != null) {
        TopDocs results = index.search(parser.parse(query), null, 100000);
        
        resultMap.put("number-hits", results.totalHits);
        for(ScoreDoc doc : results.scoreDocs) {
          Document document = index.doc(doc.doc);
          Map<String, Object> result = new LinkedHashMap<String, Object>();
          result.put("score", doc.score);
          result.put("path", document.get("path"));
          resultList.add(result);
        }
        
      }
      
      return resultMap;
    }
  }
  
  void setIndex(IndexSearcher index) {
    this.index = index;
  }
  
  void setParser(QueryParser parser) {
    this.parser = parser;
  }
  
  private void close(Closeable... toClose) {
    if(toClose != null) {
      for(Closeable close : toClose) {
        try {
          if(close != null) {
            close.close();
          }
        } catch(Exception e) {}
      }
    }
  }
}
