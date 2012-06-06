package com.meltmedia.cadmium.cli;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.core.history.HistoryEntry;

@Parameters(commandDescription = "Lists history for a given site in human readable form.", separators="=")
public class HistoryCommand {
  private static final Pattern URI_PATTERN = Pattern.compile("^(http[s]{0,1}://.*)$");

  @Parameter(names="-n", description="Limits number of history items returned.", required=false)
  private Integer limit = -1;
  
  @Parameter(names="-r", description="Filters out non revertible history items from list.", required=false)
  private boolean filter = false;
  
  @Parameter(description="Site", required=true)
  private List<String> site;
  
  public void execute() throws Exception {
    String siteUri = site.get(0);
    Matcher siteMatcher = URI_PATTERN.matcher(siteUri);
    if(siteMatcher.matches()) {
      siteUri = siteMatcher.group(1);
      
      System.out.println("Showing history for "+site+":");
      
      List<HistoryEntry> history = getHistory(siteUri, limit, filter);
            
      displayHistory(history, false, null);
    } else {
      System.err.println("Invalid value for site parameter!");
      System.exit(1);
    }
  }

  public static void displayHistory(List<HistoryEntry> history, boolean filter, Integer limitHistory) {
    if(history != null && history.size() > 0) {
      System.console().format("%7s|%12s|%7s|%14s|%24s|%44s|%24s|%6s|%10s\n", "Index", "Date", "Time", "User", "Branch", "Revision", "Time Live", "Maint", "Revertible");
      for(int i=0; i<156; i++) {
        System.out.print("-");
      }
      System.out.println();
      boolean showing = false;
      for(HistoryEntry entry : history) {
        if(!filter || entry.isRevertible()) {
          if(limitHistory == null || limitHistory-- > 0) {
            showing = true;
            System.console().format("%7d|%4tm/%<2td/%<4tY|%<4tH:%<2tM|%14s|%24s|%44s|%24s|%6b|%10b\n",
                entry.getIndex(),
                entry.getTimestamp(),
                entry.getOpenId(),
                entry.getBranch(),
                entry.getRevision(),
                formatTimeLive(entry.getTimeLive() == 0 ? System.currentTimeMillis() - entry.getTimestamp().getTime() : entry.getTimeLive()),
                entry.isMaintenance(),
                entry.isRevertible());
            printComments(entry.getComment());
          }
        }
      }
      if(!showing) {
        System.out.println("No history to show");
      }
    } else {
      System.out.println("No history to show");
    }
  }

  public static List<HistoryEntry> getHistory(String siteUri, int limit, boolean filter)
      throws URISyntaxException, IOException, ClientProtocolException {

    if(!siteUri.endsWith("/system/history")) {
      siteUri += "/system/history";
    }
    
    List<HistoryEntry> history = null;
    
    HttpClient httpClient = new DefaultHttpClient();
    HttpGet get = null;
    try {
      URIBuilder uriBuilder = new URIBuilder(siteUri);
      if(limit > 0) {
        uriBuilder.addParameter("limit", limit+"");
      }
      if(filter) {
        uriBuilder.addParameter("filter", filter+"");
      }
      URI uri = uriBuilder.build();
      get = new HttpGet(uri);
      
      HttpResponse resp = httpClient.execute(get);
      if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        HttpEntity entity = resp.getEntity();
        if(entity.getContentType().getValue().equals("application/json")) {
          String responseContent = EntityUtils.toString(entity);
          
          history = new Gson().fromJson(responseContent, new TypeToken<List<HistoryEntry>>() {}.getType());
        } else {
          System.err.println("Invalid response content type ["+entity.getContentType().getValue()+"]");
          System.exit(1);
        }
      } else {
        System.err.println("Request failed due to a ["+resp.getStatusLine().getStatusCode()+":"+resp.getStatusLine().getReasonPhrase()+"] response from the remote server.");
        System.exit(1);
      }
    } finally {
      if(get != null) {
        get.releaseConnection();
      }
    }
    return history;
  }

  private static void printComments(String comment) {
    int index = 0;
    int nextIndex = 154;
    while(index < comment.length()) {
      nextIndex = nextIndex <= comment.length() ? nextIndex : comment.length();
      String commentSegment = comment.substring(index, nextIndex);
      int lastSpace = commentSegment.lastIndexOf(' ');
      int lastNewLine = commentSegment.indexOf('\n');
      char lastChar = ' ';
      if(nextIndex < comment.length() ) {
        lastChar = comment.charAt(nextIndex);
      }
      if(lastNewLine > 0) {
        nextIndex = index + lastNewLine;
        commentSegment = comment.substring(index, nextIndex);
      } else
      if(Character.isWhitespace(lastChar)) {
        
      } else
      if(lastSpace > 0) {
        nextIndex = index + lastSpace;
        commentSegment = comment.substring(index, nextIndex);
      }
      System.out.println("  " + commentSegment);
      index = nextIndex;
      if(lastNewLine > 0 || lastSpace > 0) {
        index++;
      }
      nextIndex = index + 154;
    }
  }

  private static String formatTimeLive(long timeLive) {
    String timeString = "ms";
    timeString = (timeLive % 1000) + timeString;
    timeLive = timeLive / 1000;
    if(timeLive > 0) {
      timeString = (timeLive % 60) + "s" + timeString;
      timeLive = timeLive / 60;
      if(timeLive > 0) {
        timeString = (timeLive % 60) + "m" + timeString;
        timeLive = timeLive / 60;
        if(timeLive > 0) {
          timeString = (timeLive % 24) + "h" + timeString;
          timeLive = timeLive / 24;
          if(timeLive > 0) {
            timeString = (timeLive) + "d" + timeString;
          }
        }
      }
    }
    return timeString;
  }
  
}
