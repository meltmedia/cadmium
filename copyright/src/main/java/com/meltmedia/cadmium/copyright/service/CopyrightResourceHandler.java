/**
 *    Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.copyright.service;

import jodd.jerry.Jerry;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Calendar;

/**
 * {@link ResourceHandler} that updates all html elements that have 
 * an attribute of <code>data-cadmium="copyright"</code> with the current year.
 * 
 * @author John McEntire
 *
 */
public class CopyrightResourceHandler implements ResourceHandler {
	private final Logger log = LoggerFactory.getLogger(getClass());

	protected Integer year;
  private Jerry.JerryParser jerryParser = null;
	
	/**
	 * Initializes this instance with the current year.
	 */
	public CopyrightResourceHandler() {
	  year = Calendar.getInstance().get(Calendar.YEAR);
	  log.debug("Updating all copyrights on site to {}", year);
	}
	
	/**
	 * Updates the copyright dates in the given file.
	 */
	@Override
	public void handleFile(File htmlFile) {
    if(jerryParser == null){
      jerryParser = Jerry.jerry().enableHtmlMode();
      jerryParser.getDOMBuilder().setCaseSensitive(false);
      jerryParser.getDOMBuilder().setParseSpecialTagsAsCdata(true);
      jerryParser.getDOMBuilder().setSelfCloseVoidTags(false);
      jerryParser.getDOMBuilder().setConditionalCommentExpression(null);
      jerryParser.getDOMBuilder().setEnableConditionalComments(false);
      jerryParser.getDOMBuilder().setImpliedEndTags(false);
    }
	  log.trace("Handling file {}", htmlFile);
	  try {
	    String fileContents = FileUtils.readFileToString(htmlFile);
	    Jerry html = jerryParser.parse(fileContents);
	    Jerry selector = html.$("[data-cadmium='copyright']");
	    log.debug("Found {} copyright tags.", selector.length());
	    if(selector.length() > 0) {
	      selector.text(year.toString());
	      log.trace("Writing updated file {}",htmlFile);
	      FileUtils.writeStringToFile(htmlFile, html.html(), false);
	    }
	  } catch(Throwable t) {
	    log.warn("Failed to update file "+htmlFile, t);
	  }
	}

}
