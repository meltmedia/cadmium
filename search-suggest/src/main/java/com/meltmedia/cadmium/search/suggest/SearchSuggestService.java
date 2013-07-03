package com.meltmedia.cadmium.search.suggest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.lucene.search.suggest.Lookup.LookupResult;
import com.google.inject.Inject;
import com.meltmedia.cadmium.core.CadmiumApiEndpoint;

@CadmiumApiEndpoint
@Path("/suggest")
public class SearchSuggestService {

	@Inject
	protected SuggesterProvider preprocessor;
	static final int MAX_RESULTS = 20; 
	
	@GET
  @Produces("application/json")
  public Map<String, Object> searchSuggest(@QueryParam("query") String query, @QueryParam("path") String path, @QueryParam("maxResults") @DefaultValue("20") Integer maxResults)
      throws Exception {
		
    Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
    List<LookupResult> results = preprocessor.getSuggester().lookup(query, false, maxResults == null? MAX_RESULTS : maxResults);
    resultMap.put(query, results);        
    
    return resultMap;
  }
	
}
