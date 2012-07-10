package com.meltmedia.cadmium.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;

public interface IndexSearcherProvider {
  public IndexSearcher startSearch();
  public void endSearch();
  public Analyzer getAnalyzer();
}
