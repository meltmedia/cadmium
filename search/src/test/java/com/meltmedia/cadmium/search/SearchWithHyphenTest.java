package com.meltmedia.cadmium.search;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Test to make sure that search terms with embedded hyphens and other lucene special characters get escaped.
 *
 * The lucene special characters are "+ - && || ! ( ) { } [ ] ^ " ~ * ? : \"
 *
 * @author jmcentire
 */
@RunWith(Parameterized.class)
public class SearchWithHyphenTest {


  private IndexSearcherProvider provider;

  private static final Analyzer analyzer = new CadmiumAnalyzer(Version.LUCENE_43);
  private static final String termPrefix = "prefix";
  private static final String termSuffix = "suffix";

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {"+"},
        {"-"},
        {"&&"},
        {"||"},
        {"!"},
        {"("},
        {")"},
        {"{"},
        {"}"},
        {"["},
        {"]"},
        {"^"},
        {"\""},
        {"~"},
        {"*"},
        {"?"},
        {":"},
        {"\\"}
    });
  }

  private String delim;

  public SearchWithHyphenTest(String delim) {
    this.delim = delim;
  }

  @Before
  public void setupForTest() throws Exception {
    provider = mock(IndexSearcherProvider.class);
    createService();
    doReturn(new IndexSearcher(reader)).when(provider).startSearch();
    doNothing().when(provider).endSearch();
    doReturn(analyzer).when(provider).getAnalyzer();
  }

  @Test
  public void runTest() throws Exception {
    Map<String, Object> results =  service.search(termPrefix + delim + termSuffix, "/path/to");
    assertNotNull(delim, results);
    assertEquals(delim, 1, results.get("number-hits"));
    assertNotNull(delim, results.get("results"));
    List<Map<String, Object>> resultList = (List<Map<String, Object>>) results.get("results");
    assertEquals(delim, "/path/to/file3", resultList.get(0).get("path"));
  }

  private Directory indexDir = null;
  private SearchService service = null;
  private IndexReader reader = null;

  private void createService() throws Exception {
    indexDir = new RAMDirectory();

    IndexWriter iwriter = null;

    try {
      iwriter = new IndexWriter(indexDir, new IndexWriterConfig(Version.LUCENE_43, analyzer).setRAMBufferSizeMB(5));
      iwriter.deleteAll();
      writeIndex(iwriter);
    } finally {
      IOUtils.closeQuietly(iwriter);
    }

    reader = DirectoryReader.open(indexDir);

    service = new SearchService();
    service.provider = provider;
  }

  private void writeIndex(IndexWriter iwriter) throws Exception {
    String contentTmpl = "This is the content template. The ${term}, will be embedded.";
    String title = "This is a generic title.";
    String content[] = {termPrefix, termSuffix, termPrefix+" "+termSuffix, termPrefix+delim+termSuffix, "Bogus"};

    for(int i=0; i<content.length; i++) {
      String textContent = contentTmpl.replace("${term}", content[i]);
      Document doc = new Document();
      doc.add(new Field("title", title, Field.Store.YES, Field.Index.ANALYZED));
      doc.add(new Field("content", textContent, Field.Store.YES, Field.Index.ANALYZED));
      doc.add(new Field("path", "/path/to/file"+i, Field.Store.YES, Field.Index.ANALYZED));
      iwriter.addDocument(doc);
    }
  }

}
