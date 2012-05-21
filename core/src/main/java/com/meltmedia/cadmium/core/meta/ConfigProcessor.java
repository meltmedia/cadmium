package com.meltmedia.cadmium.core.meta;


public interface ConfigProcessor {
  public void processFromDirectory(String metaDir) throws Exception;
  public void makeLive();
}
