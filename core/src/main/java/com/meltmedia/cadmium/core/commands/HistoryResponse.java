package com.meltmedia.cadmium.core.commands;

import java.util.List;

import com.meltmedia.cadmium.core.history.HistoryEntry;

public class HistoryResponse {

  private List<HistoryEntry> history;

  public HistoryResponse(List<HistoryEntry> history) {
    this.history = history;
  }

  public List<HistoryEntry> getHistory() {
    return history;
  }

  public void setHistory(List<HistoryEntry> history) {
    this.history = history;
  }

}
