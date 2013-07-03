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
package com.meltmedia.cadmium.search;

import com.google.inject.Inject;
import com.meltmedia.cadmium.core.CadmiumApiEndpoint;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@CadmiumApiEndpoint
@Path("/search")
public class SearchService
{
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  @Inject
  private IndexSearcherProvider provider;
    
	@GET
  @Produces("application/json")
  public Map<String, Object> search(@QueryParam("query") String query,@QueryParam("path") String path)
      throws Exception {
    Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
    
    // TODO build new query using path
    resultMap = buildSearchResults(query, path);
    
    return resultMap;
  }
  
  private Map<String,Object> buildSearchResults(final String query, final String path) throws Exception {
    logger.info("Running search for [{}]", query);
    final Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
    
    new SearchTemplate(provider) {
      public void doSearch(IndexSearcher index) throws IOException,
          ParseException {
        QueryParser parser = createParser(getAnalyzer());
        
        resultMap.put("number-hits", 0);
        
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        
        resultMap.put("results", resultList);
        
        if (index != null && parser != null) {
          Query query1 = parser.parse(query);
          if(StringUtils.isNotBlank(path)) {
          	Query pathPrefix = new PrefixQuery(new Term("path", path));
          	BooleanQuery boolQuery = new BooleanQuery();
          	boolQuery.add(pathPrefix, Occur.MUST);
          	boolQuery.add(query1, Occur.MUST);
          	query1 = boolQuery;
          }
          TopDocs results = index.search(query1, null, 100000);
          QueryScorer scorer = new QueryScorer(query1);
          Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(), new SimpleHTMLEncoder(), scorer);
    
          logger.info("", results.totalHits);
          resultMap.put("number-hits", results.totalHits);
          
          for (ScoreDoc doc : results.scoreDocs) {
            Document document = index.doc(doc.doc);
            String content = document.get("content");
            String title = document.get("title");

            Map<String, Object> result = new LinkedHashMap<String, Object>();
            String excerpt = "";
          
            try {
              excerpt = highlighter.getBestFragments(parser.getAnalyzer().tokenStream(null, new StringReader(content)), content, 3, "...");
              excerpt = fixExcerpt(excerpt);
              
              result.put("excerpt", excerpt);
            } catch(Exception e) {
              logger.debug("Failed to get search excerpt from content.", e);
              
              try {
                excerpt = highlighter.getBestFragments(parser.getAnalyzer().tokenStream(null, new StringReader(title)), title, 1, "...");
                excerpt = fixExcerpt(excerpt);
                
                result.put("excerpt", excerpt);
              } catch(Exception e1) {
                logger.debug("Failed to get search excerpt from title.", e1);
                
                result.put("excerpt", "");
              }
            }
            
            result.put("score", doc.score);
            result.put("title", title);
            result.put("path", document.get("path"));
            
            resultList.add(result);
          }
        }

      }
    }.search();

    return resultMap;
  }

  private static String fixExcerpt(String excerpt) {
    if(excerpt != null) {
      excerpt = excerpt.replace("\n", "");
      String newExcerpt = excerpt.replace("  ", " ");
      while(!excerpt.equals(newExcerpt)) {
        excerpt = newExcerpt;
        newExcerpt = excerpt.replace("  ", " ");
      }
    }
    return excerpt;
  }
  
  QueryParser createParser( Analyzer analyzer ) {
    return new MultiFieldQueryParser(Version.LUCENE_43, new String[]{"title", "content"}, analyzer);
  }
  
  void setIndexSearchProvider(IndexSearcherProvider provider) {
    this.provider = provider;
  }
}
