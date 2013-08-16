package com.meltmedia.cadmium.search.suggest;

import javax.inject.Singleton;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.search.suggest.analyzing.FuzzySuggester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.search.SearchPreprocessor;


/**
 * 
 * @author Brian Barr
 *
 */
@Singleton
public class SuggestSearchPreprocessor implements SearchPreprocessor, SuggesterProvider {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected AnalyzingSuggester stagedSuggester;
	protected AnalyzingSuggester liveSuggester;
	
	@Override
	public void makeLive() {
		liveSuggester = stagedSuggester;		
		stagedSuggester = null;
	}

	@Override
	public void process(IndexReader reader, Analyzer analyzer, String field) throws Exception {
		
		logger.info("Pulling out suggested search terms.");
		Dictionary dictionary = new LuceneDictionary(reader, field);		
		AnalyzingSuggester suggester = new AnalyzingSuggester(analyzer); 
		suggester.build(dictionary);
		stagedSuggester = suggester;		            		
	}

	@Override
	public AnalyzingSuggester getSuggester() {
		return liveSuggester;
	}

	public void setStagedSuggester(AnalyzingSuggester stagedSuggester) {
		this.stagedSuggester = stagedSuggester;
	}

	public void setLiveSuggester(AnalyzingSuggester liveSuggester) {
		this.liveSuggester = liveSuggester;
	}

}
