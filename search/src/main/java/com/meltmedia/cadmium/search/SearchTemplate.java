package com.meltmedia.cadmium.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;

public abstract class SearchTemplate {
  private IndexSearcherProvider provider;
  public SearchTemplate( IndexSearcherProvider provider ) {
    this.provider = provider;
  }
  public SearchTemplate search() throws Exception {
    try {
      doSearch(provider.startSearch());
    }
    finally {
      provider.endSearch();
    }
    return this;
  }
  
  public Analyzer getAnalyzer() {
    return provider.getAnalyzer();
  }
  
  public abstract void doSearch( IndexSearcher index)
    throws Exception;
}
