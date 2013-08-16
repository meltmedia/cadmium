package com.meltmedia.cadmium.search.suggest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.junit.Before;
import org.junit.Test;

public class SearchSuggestServiceTest {

	private SuggesterProvider provider;
	private SearchSuggestService service;
	
	private final String HAS_RESULTS_QUERY = "hasResultsQuery";
	private final String NO_RESULTS_QUERY = "noResultsQuery";
	private final String path = "content";
	private String RESULT_TEXT_1 = "resultText_1";
	private String RESULT_TEXT_2 = "resultText_2";
	private String RESULT_TEXT_3 = "resultText_3";
	private String RESULT_TEXT_4 = "resultText_4";
	
	List<LookupResult> hasResults;
	Set<String> expectedResultSetWithResults;
	
	List<LookupResult> noResults;
	Set<String> expectedResultSetNoResults;
	
	
	@Before
	public void setup() {	
		
		AnalyzingSuggester suggester = mock(AnalyzingSuggester.class);
		provider = mock(SuggesterProvider.class);
		service = new SearchSuggestService();
		service.setProvider(provider);
		
		hasResults = new ArrayList<LookupResult>();
		LookupResult result1 = new LookupResult(RESULT_TEXT_1, 1);
		LookupResult result2 = new LookupResult(RESULT_TEXT_2, 2);
		LookupResult result3 = new LookupResult(RESULT_TEXT_3, 3);
		LookupResult result4 = new LookupResult(RESULT_TEXT_4, 4);
		hasResults.add(result1);
		hasResults.add(result2);
		hasResults.add(result3);
		hasResults.add(result4);
		
		expectedResultSetWithResults = new TreeSet<String>();
		expectedResultSetWithResults.add(RESULT_TEXT_1);
		expectedResultSetWithResults.add(RESULT_TEXT_2);
		expectedResultSetWithResults.add(RESULT_TEXT_3);
		expectedResultSetWithResults.add(RESULT_TEXT_4);
		
		noResults = new ArrayList<LookupResult>();
		
		expectedResultSetNoResults = new TreeSet<String>();
		
		when(provider.getSuggester()).thenReturn(suggester);
		when(suggester.lookup(eq(HAS_RESULTS_QUERY), eq(false), eq(SearchSuggestService.MAX_RESULTS))).thenReturn(hasResults);
		when(suggester.lookup(eq(NO_RESULTS_QUERY), eq(false), eq(SearchSuggestService.MAX_RESULTS))).thenReturn(noResults);
	}
	
	@Test
	public void withResultsTest() throws Exception {	
		
		Map<String, Object> hasResultMap = service.searchSuggest(HAS_RESULTS_QUERY, path, SearchSuggestService.MAX_RESULTS);				
		assertEquals("Results map should not be empty.", hasResultMap.get(SearchSuggestService.RESULT_KEY), expectedResultSetWithResults);		
	}
	
	@Test
	public void noResultsTest() throws Exception {
		
		Map<String, Object> noResultMap = service.searchSuggest(NO_RESULTS_QUERY, path, SearchSuggestService.MAX_RESULTS);
		assertEquals("The results map should be empty!", noResultMap.get(SearchSuggestService.RESULT_KEY), expectedResultSetNoResults);
	}
	
	@Test
	public void nullMaxResultsTest() throws Exception {
		
		Map<String, Object> hasResultMap = service.searchSuggest(HAS_RESULTS_QUERY, path, null);				
		assertEquals("Results map should not be empty.", hasResultMap.get(SearchSuggestService.RESULT_KEY), expectedResultSetWithResults);
	}
}
