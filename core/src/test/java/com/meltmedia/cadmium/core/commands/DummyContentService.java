package com.meltmedia.cadmium.core.commands;

import java.io.IOException;
import java.io.InputStream;

import com.meltmedia.cadmium.core.ContentService;

public class DummyContentService implements ContentService {
  
  public boolean switched = false;

  @Override
  public void switchContent() {
    switched = true;
  }

	@Override
	public String getContentRoot() {
		// TODO Auto-generated method stub
		return null;
	}

  @Override
  public InputStream getResourceAsStream(String path) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

}
