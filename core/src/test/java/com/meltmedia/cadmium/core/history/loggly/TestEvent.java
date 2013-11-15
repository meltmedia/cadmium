package com.meltmedia.cadmium.core.history.loggly;

/**
 * com.meltmedia.cadmium.core.history.loggly.TestEvent
 *
 * @author jmcentire
 */
public class TestEvent extends Event {

  private String field1;
  private String field2;
  private String type;

  public TestEvent() {}

  public String getField1() {
    return field1;
  }

  public void setField1(String field1) {
    this.field1 = field1;
  }

  public String getField2() {
    return field2;
  }

  public void setField2(String field2) {
    this.field2 = field2;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
