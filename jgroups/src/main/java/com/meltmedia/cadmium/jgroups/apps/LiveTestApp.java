package com.meltmedia.cadmium.jgroups.apps;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.jgroups.JChannel;
import org.jgroups.Message;

import com.meltmedia.cadmium.jgroups.ContentService;
import com.meltmedia.cadmium.jgroups.ContentServiceListener;
import com.meltmedia.cadmium.jgroups.CoordinatedWorker;
import com.meltmedia.cadmium.jgroups.CoordinatedWorkerListener;
import com.meltmedia.cadmium.jgroups.SiteDownService;
import com.meltmedia.cadmium.jgroups.receivers.UpdateChannelReceiver;

public class LiveTestApp implements CoordinatedWorker, ContentService, SiteDownService {
  
  private CoordinatedWorkerListener listener;
  private ContentServiceListener contentListener;

  @Override
  public void beginPullUpdates(Map<String, String> properties) {
    new Thread(new Runnable() {
      public void run() {
        System.out.println("Pulling Updates!!!");
        System.out.print("Hit enter when you are ready to move on or enter the word 'kill' to fail the update! ");
        try {
          String response = readLine();
          if(response != null && response.trim().equalsIgnoreCase("kill")) {
            listener.workFailed();
            return;
          }
        } catch(Exception e) {}
        System.out.println("Waiting!!!");
        listener.workDone(null);
      }
    }).start();
  }

  @Override
  public void switchContent(String newDir) {
    new Thread(new Runnable() {
      public void run() {
        System.out.println("Switching content!!!!");
        contentListener.doneSwitching();
      }
    }).start();
  }

  @Override
  public void killUpdate() {
    new Thread(new Runnable() {
      public void run() {
        System.out.println("Killing Update!!!!");
      }
    }).start();
  }

  @Override
  public void takeSiteDown() {
    System.out.println("Taking Site Down!!!!");
  }

  @Override
  public void bringSiteUp() {
    System.out.println("Bringing Site Up!!!!");
  }

  @Override
  public void setListener(ContentServiceListener listener) {
    this.contentListener = listener;
  }

  @Override
  public void setListener(CoordinatedWorkerListener listener) {
    this.listener = listener;
  }
  
  public synchronized String readLine() throws Exception {
    String cmd = System.console().readLine();
    return cmd;
  }

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    URL propsUrl = LiveTestApp.class.getClassLoader().getResource("tcp.xml");
    String configFile = propsUrl.toString();
    System.out.println("Here is the configuration : {"+configFile+"}");
    JChannel cnl = new JChannel(propsUrl);
    cnl.connect("JGroupsContentUpdateTestingChannel");
    LiveTestApp worker = new LiveTestApp();
    UpdateChannelReceiver receiver = new UpdateChannelReceiver(cnl, worker, worker, worker);
    if(receiver.getMyState() == UpdateChannelReceiver.UpdateState.IDLE){
      System.out.print("Hit enter to continue: ");
      worker.readLine();
      if(receiver.getMyState() == UpdateChannelReceiver.UpdateState.IDLE){
        cnl.send(new Message(null, null, "UPDATE"));
      }
    }
    while(true) {
      try{
        Thread.sleep(1000l);
      } catch(Exception e){}
      if(receiver.getMyState() == UpdateChannelReceiver.UpdateState.IDLE) {
        break;
      }
    }
    
  }

}
