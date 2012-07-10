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

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;

import com.google.inject.Inject;

@Path("/search")
public class SearchService
{

  @Inject
  private IndexSearcherProvider provider;
  
  @GET
  @Produces("application/json")
  public Map<String, Object> search(final @QueryParam("query") String query)
      throws Exception {
    final Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
    new SearchTemplate(provider) {
      public void doSearch(IndexSearcher index) throws IOException,
          ParseException {
        QueryParser parser = createParser(getAnalyzer());
        resultMap.put("number-hits", 0);
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        resultMap.put("results", resultList);
        if (index != null && parser != null) {
          TopDocs results = index.search(parser.parse(query), null, 100000);

          resultMap.put("number-hits", results.totalHits);
          for (ScoreDoc doc : results.scoreDocs) {
            Document document = index.doc(doc.doc);
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("score", doc.score);
            result.put("path", document.get("path"));
            resultList.add(result);
          }
        }

      }
    }.search();

    return resultMap;
  }
  
  QueryParser createParser( Analyzer analyzer ) {
    return new MultiFieldQueryParser(Version.LUCENE_36, new String[]{"title", "content"}, analyzer);
  }
  
  void setIndexSearchProvider(IndexSearcherProvider provider) {
    this.provider = provider;
  }
}
