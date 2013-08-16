package com.meltmedia.cadmium.search.suggest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.meltmedia.cadmium.core.CadmiumApiEndpoint;

@CadmiumApiEndpoint
@Path("/suggest")
public class SearchSuggestService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	protected SuggesterProvider provider;
	static final int MAX_RESULTS = 20; 
	static final String RESULT_KEY = "terms";
	
	@GET
  @Produces("application/json")
  public Map<String, Object> searchSuggest(@QueryParam("query") String query, @QueryParam("path") String path, @QueryParam("maxResults") @DefaultValue("20") Integer maxResults)
      throws Exception {
		
    Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
    logger.info("provider: {}", provider);
    logger.info("suggester: {}", provider.getSuggester());
    List<LookupResult> results = provider.getSuggester().lookup(query, false, maxResults == null? MAX_RESULTS : maxResults);
    
    // Make the results more usable to consumer.
    Set<String> resultSet = new TreeSet<String>();
    resultMap.put(RESULT_KEY, resultSet);
    
    if(results != null) {
    	for(LookupResult result : results) {    		    		
    		resultSet.add(result.key.toString());
    	}    	
    }
    return resultMap;
  }
	
	public void setProvider(SuggesterProvider provider) {
		this.provider = provider;
	}

}
