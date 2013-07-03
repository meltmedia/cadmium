package com.meltmedia.cadmium.search.suggest;

import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;

public interface SuggesterProvider {
	public AnalyzingSuggester getSuggester();
}
