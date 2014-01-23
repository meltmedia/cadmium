package com.meltmedia.cadmium.core.history.loggly;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.meltmedia.cadmium.core.history.HistoryEntry.EntryType;
import org.junit.Test;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * com.meltmedia.cadmium.core.history.loggly.EventTest
 *
 * @author jmcentire
 */
public class EventTest {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy/MM/dd HH:mm:ss.SSS").create();

  @Test
  public void testSerialization() {
    HistoryEvent evt = new HistoryEvent();
    evt.setBranch("b1");
    evt.setComment("blah");
    evt.setMaintenance(true);
    evt.setRepoUrl("repo1");
    evt.setRevision("rev1");
    evt.setTimestamp(new Date());
    evt.setType(EntryType.CONFIG);
    evt.setUserId("id1");
    evt.setDomain("domain");
    evt.setEnvironment("env");

    String jsonStr = GSON.toJson(evt);

    assertNotNull(jsonStr);
    System.out.println(jsonStr);

    HistoryEvent evt2 = GSON.fromJson(jsonStr, HistoryEvent.class);

    assertNotNull(evt2);

    assertEquals(evt, evt2);
  }

  @Test
  public void testOtherEvent() {
    TestEvent evt = new TestEvent();
    evt.setField1("foo");
    evt.setField2("barr");
    evt.setTimestamp(new Date());
    evt.setType("TEST");
    evt.setDomain("domain");
    evt.setEnvironment("env");

    String jsonStr = GSON.toJson(evt);

    assertNotNull(jsonStr);
    System.out.println(jsonStr);

    TestEvent evt2 = GSON.fromJson(jsonStr, TestEvent.class);

    assertNotNull(evt2);

    assertEquals(evt, evt2);

  }
}
