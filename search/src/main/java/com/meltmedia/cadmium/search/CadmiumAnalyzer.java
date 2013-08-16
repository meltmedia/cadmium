package com.meltmedia.cadmium.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import java.io.Reader;

/**
 * com.meltmedia.cadmium.search.CadmiumAnalyzer
 *
 * @author jmcentire
 */
public class CadmiumAnalyzer extends Analyzer {
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    return new Analyzer
  }
}
