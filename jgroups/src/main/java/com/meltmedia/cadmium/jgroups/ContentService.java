package com.meltmedia.cadmium.jgroups;


public interface ContentService {
  public void switchContent(String newDir);
  public void setListener(ContentServiceListener listener);
}
