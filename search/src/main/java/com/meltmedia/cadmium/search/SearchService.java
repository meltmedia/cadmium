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
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.Inject;

@Path("/search")
public class SearchService
{
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Inject
  private IndexSearcherProvider provider;
  
  
  public Map<String, Object> search(final @QueryParam("query") String query)
      throws Exception {
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
          TopDocs results = index.search(parser.parse(query), null, 100000);
          logger.info("", results.totalHits);
          resultMap.put("number-hits", results.totalHits);
          for (ScoreDoc doc : results.scoreDocs) {
            Document document = index.doc(doc.doc);
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("score", doc.score);
            result.put("title", document.get("title"));
            result.put("path", document.get("path"));
            resultList.add(result);
          }
        }

      }
    }.search();

    return resultMap;
  }
  
  @GET
  @Produces("application/json")
  public String searchResponse(final @QueryParam("query") String query) throws Exception {
    return new Gson().toJson(search(query));
  }
  
  QueryParser createParser( Analyzer analyzer ) {
    return new MultiFieldQueryParser(Version.LUCENE_36, new String[]{"title", "content"}, analyzer);
  }
  
  void setIndexSearchProvider(IndexSearcherProvider provider) {
    this.provider = provider;
  }
}
