package com.meltmedia.cadmium.core.commands;

public class HistoryRequest extends AbstractMessageBody {

  private Integer limit;
  private boolean filter;

  public Integer getLimit() {
    return limit;
  }

  public boolean getFilter() {
    return filter;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public void setFilter(boolean filter) {
    this.filter = filter;
  }

}
