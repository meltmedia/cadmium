package com.meltmedia.cadmium.search;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.regex.Pattern;

/**
 * com.meltmedia.cadmium.search.CadmiumAnalyzer
 *
 * @author jmcentire
 */
public class CadmiumAnalyzer extends StopwordAnalyzerBase {

  public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

  public CadmiumAnalyzer(Version matchVersion) {
    super(matchVersion, STOP_WORDS_SET);
  }
  @Override
  protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
    WhitespaceTokenizer src = new WhitespaceTokenizer(matchVersion, reader);
    TokenStream tok = new StopFilter(matchVersion, src, stopwords);
    //tok = new WordDelimiterFilter(src, WordDelimiterFilter.PRESERVE_ORIGINAL, null);
    tok = new LowerCaseFilter(matchVersion, tok);
    tok = new StopFilter(matchVersion, tok, stopwords);
    tok = new PatternReplaceFilter(tok, Pattern.compile("(^(\\.|,|\\:|\\;))|((\\.|,|\\:|\\;)$)"), "", true);
    return new TokenStreamComponents(src, tok);
  }
}
