package com.meltmedia.cadmium.core.meta;

public class DummyConfigProcessor implements ConfigProcessor {

  public boolean throwError = false;
  public boolean processed = false;
  public boolean live = false;
  
  @Override
  public void processFromDirectory(String metaDir) throws Exception {
    if(throwError) {
      throw new Exception("");
    }
    processed = true;
  }

  @Override
  public void makeLive() {
    live = true;
  }

}
