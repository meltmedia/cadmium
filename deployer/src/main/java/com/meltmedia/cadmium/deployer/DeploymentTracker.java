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
package com.meltmedia.cadmium.deployer;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.meltmedia.cadmium.core.messaging.ChannelMember;
import com.meltmedia.cadmium.core.messaging.MembershipTracker;
import com.meltmedia.cadmium.core.messaging.Message;
import com.meltmedia.cadmium.core.messaging.MessageSender;
import com.meltmedia.cadmium.deployer.JBossUtil.DeploymentState;
import com.meltmedia.cadmium.deployer.SitePinger.PingStatus;

/**
 * Maintains status information for all deployments that have been made since an instance of this class has been allocated.
 * 
 * @author John McEntire
 *
 */
public class DeploymentTracker implements Closeable {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private List<DeploymentStatus> deploymentStatuses = new ArrayList<DeploymentStatus>();
  private Map<DeploymentStatus, SitePinger> sitePingers = new HashMap<DeploymentStatus, SitePinger>();
  private List<UndeploymentStatus> undeploymentStatuses = new ArrayList<UndeploymentStatus>();
  private Timer scheduler = new Timer();
  
  @Inject
  protected MessageSender sender;
  
  @Inject
  protected MembershipTracker memberTracker;
  
  public DeploymentTracker() {
    scheduler.schedule(new DeploymentTrackerTask(), 10000l, 10000l);
  }
  
  /**
   * Fetches a {@link DeploymentStatus} instance for the requested deployment.
   * 
   * @param domain The domain specified for the deployment.
   * @param contextRoot The contextRoot for the deployment.
   * @return The instance of the {@link DeploymentStatus} for the deploy requested if found or null if not found.
   * @throws CadmiumDeploymentException Thrown if the {@link DeploymentStatus} found has a reference to an exception. 
   */
  public DeploymentStatus isDeploymentComplete(String domain, String contextRoot) throws CadmiumDeploymentException {
    DeploymentStatus status = getDeployment(domain, contextRoot);
    if(status != null) {
      if(status.exception != null) {
        throw status.exception;
      }
      return status;
    }
    return null;
  }
  
  /**
   * Gets a {@link DeploymentStatus} for the given domain and contextRoot.
   * 
   * @param domain
   * @param contextRoot
   * @return
   */
  public DeploymentStatus getDeployment(String domain, String contextRoot) {
    DeploymentStatus keyStatus = getEmptyDeploymentStatus(domain, contextRoot);
    if(deploymentStatuses.contains(keyStatus)) {
      return deploymentStatuses.get(deploymentStatuses.indexOf(keyStatus));
    }
    return null;
  }
  
  /**
   * Gets a {@link UndeploymentStatus} for the given domain and contextRoot.
   * 
   * @param domain
   * @param contextRoot
   * @return
   */
  public UndeploymentStatus getUndeployment(String domain, String contextRoot) {
    UndeploymentStatus keyStatus = getEmptyUndeploymentStatus(domain, contextRoot);
    if(undeploymentStatuses.contains(keyStatus)) {
      return undeploymentStatuses.get(undeploymentStatuses.indexOf(keyStatus));
    }
    return null;
  }
  
  /**
   * Initializes or clears a {@link DeploymentStatus} for the given domain and contextRoot. 
   * This method make sure that there is only one instance of the {@link DeploymentStatus} 
   * for the given domain and contextRoot.
   * 
   * @param domain
   * @param contextRoot
   * @return
   */
  public synchronized DeploymentStatus initializeDeployment(String domain, String contextRoot) {
    DeploymentStatus status = getDeployment(domain, contextRoot);
    if(status == null) {
      status = getEmptyDeploymentStatus(domain, contextRoot);
      deploymentStatuses.add(status);
    } else {
      status.clear();
    }
    return status;
  }
  
  /**
   * Initializes or clears a {@link UndeploymentStatus} for the given domain and contextRoot. 
   * This method make sure that there is only one instance of the {@link UndeploymentStatus} 
   * for the given domain and contextRoot.
   * 
   * @param domain
   * @param contextRoot
   * @return
   */
  public synchronized UndeploymentStatus initializeUndeployment(String domain, String contextRoot) {
    UndeploymentStatus status = getUndeployment(domain, contextRoot);
    if(status == null) {
      status = getEmptyUndeploymentStatus(domain, contextRoot);
      undeploymentStatuses.add(status);
    } else {
      status.clear();
    }
    scheduler.schedule(new UndeploymentTrackerTask(status), 1000l);
    return status;
  }
  
  /**
   * Creates a {@link SitePinger} and associates it with a domain and contextRoot. This method 
   * does nothing if a SitePinger is already associated.
   * 
   * @param domain
   * @param contextRoot
   * @throws Exception
   */
  public synchronized void startPinging(String domain, String contextRoot) throws Exception {
    DeploymentStatus key = getDeployment(domain, contextRoot);
    if(!sitePingers.containsKey(key)) {
      sitePingers.put(key, new SitePinger(domain, contextRoot));
    }
  }
  
  /**
   * Creates an empty {@link DeploymentStatus} instance with the given domain and contextRoot.
   * 
   * @param domain
   * @param contextRoot
   * @return
   */
  private DeploymentStatus getEmptyDeploymentStatus(String domain, String contextRoot) {
    return new DeploymentStatus(domain, contextRoot);
  }
  
  /**
   * Creates an empty {@link UndeploymentStatus} instance with the given domain and contextRoot.
   * 
   * @param domain
   * @param contextRoot
   * @return
   */
  private UndeploymentStatus getEmptyUndeploymentStatus(String domain, String contextRoot) {
    return new UndeploymentStatus(domain, contextRoot);
  }
  
  /**
   * A class to track undeployments.
   * 
   * @author John McEntire
   *
   */
  public class UndeploymentStatus extends AbstractStatusTracker {

    boolean running = false;
    boolean notified = false;
    
    /**
     * Creates a new instance with the required fields.
     * @param domain 
     * @param contextRoot
     */
    public UndeploymentStatus(String domain, String contextRoot) {
      super(domain, contextRoot);
    }
    
    public void clear() {
      synchronized(this) {
        super.clear();
        running = false;
        notified = false;
      }
    }
    
  }  
  
  /**
   * Status Object for tracking a deployment.
   * 
   * @author John McEntire
   *
   */
  public class DeploymentStatus extends AbstractStatusTracker {
    private CadmiumDeploymentException exception;
    private boolean waiting = false;
    private boolean deployed = false;
    
    /**
     * Creates a new instance with the required fields.
     * @param domain 
     * @param contextRoot
     */
    public DeploymentStatus(String domain, String contextRoot) {
      super(domain, contextRoot);
    }
    
    public CadmiumDeploymentException getException() {
      return exception;
    }

    public void setException(CadmiumDeploymentException exception) {
      this.exception = exception;
    }

    public boolean isWaiting() {
      return waiting;
    }

    public void setWaiting(boolean waiting) {
      this.waiting = waiting;
    }

    public boolean isDeployed() {
      return deployed;
    }

    public void setDeployed(boolean deployed) {
      this.deployed = deployed;
    }

    /**
     * Clears previously set values for all fields except the domain and contextRoot.
     */
    public void clear() {
      synchronized(this) {
        super.clear();
        exception = null;
        waiting = false;
        cancelSitePinger();
      }
    }

    /**
     * Cancels a {@link SitePinger} instance associated with an instance of this DeploymentStatus.
     */
    public void cancelSitePinger() {
      try {
        if(sitePingers.containsKey(this)) {
          SitePinger pinger = sitePingers.remove(this);
          pinger.close();
        }
      } catch(Exception e){
        log.debug("Failed to shutdown site pinger for "+domain+":"+contextRoot, e);
      }
    }
  }
  
  /**
   * TimerTask to track a undeployment.
   * 
   * @author John McEntire
   *
   */
  public class UndeploymentTrackerTask extends TimerTask {
    
    private UndeploymentStatus status;
    
    public UndeploymentTrackerTask(UndeploymentStatus status) {
      this.status = status;
      status.running = true;
    }

    @Override
    public void run() {
      ChannelMember me = memberTracker.getMe();
      String warFileName = status.domain.replace("\\.", "_") + ".war";
      try {
        UndeployCommandAction.undeployWar(me, status, warFileName, log);
      } catch(Exception e) {
        log.warn("An error happened while undeploying "+warFileName, e);
        status.logToMember(me, "An error happened while undeploying "+warFileName);
        status.setMemberFinished(me);
      }
    }
    
  }
  
  /**
   * A TimerTask that will check the status of all deployments.
   * 
   * @author John McEntire
   *
   */
  public class DeploymentTrackerTask extends TimerTask {

    @Override
    public void run() {
      ChannelMember me = memberTracker.getMe();
      pingSites(me);
      getClusterStatuses(me);
      updateClusterUndeploymentStatuses(me);
    }
    
    /**
     * 
     */
    private void updateClusterUndeploymentStatuses(ChannelMember me) {
      for(UndeploymentStatus status : undeploymentStatuses) {
        if(status.running && !status.notified) {
          try {
            List<String> myMsgs = status.getMemberLogs(me);
            boolean finished = status.getMemberFinished(me);
            
            Message msg = new Message();
            msg.setCommand(UndeployUpdateCommandAction.DEPLOY_UPDATE_ACTION);
            msg.getProtocolParameters().put("domain", status.domain);
            msg.getProtocolParameters().put("contextRoot", status.contextRoot);
            msg.getProtocolParameters().put("finished", finished + "");
            if(myMsgs != null) {
              msg.getProtocolParameters().put("msgs", new Gson().toJson(myMsgs));
            }
          } catch(Exception e){
            log.warn("Failed to notify cluster.", e);
          }
        }
      }
    }
    
    /**
     * Requests all other nodes statuses in the cluster.
     */
    private void getClusterStatuses(ChannelMember me) {
      if(!deploymentStatuses.isEmpty() && sender != null) {
        for(DeploymentStatus status : deploymentStatuses) {
          if(status.waiting && !status.deployed) {
            try {
              DeploymentState deployState = JBossUtil.getDeploymentState(status.domain+".war");
              if(deployState != DeploymentState.NOT_STARTED) {
                status.logToMember(me, "JBoss Deployment State changed to: "+deployState);
              }
              if(deployState == DeploymentState.DEPLOYED) {
                status.deployed = true;
                status.logToMember(me, "Waiting for cadmium site to initialize.");
                startPinging(status.domain, status.contextRoot);
              }
            } catch(CadmiumDeploymentException e) {
              status.cancelSitePinger();
              status.exception = e;
              try {
                UndeployService.sendUndeployMessage(sender, status.domain, status.contextRoot, log);
              } catch (Exception e1) {
                log.warn("Failed to send undeploy message for failed deployment.", e1);
              }
            } catch(Exception e) {
              status.cancelSitePinger();
              status.exception = new CadmiumDeploymentException("Failed to deploy.",e);
              log.warn("Failed to deploy", e);
              try {
                UndeployService.sendUndeployMessage(sender, status.domain, status.contextRoot, log);
              } catch (Exception e1) {
                log.warn("Failed to send undeploy message for failed deployment.", e1);
              }
            }
          }
          
          Message msg = new Message();
          msg.setCommand(DeployUpdateCommandAction.DEPLOY_UPDATE_ACTION);
          msg.getProtocolParameters().put("domain", status.domain);
          msg.getProtocolParameters().put("contextRoot", status.contextRoot);
          
          try {
            sender.sendMessage(msg, null);
          } catch(Exception e){
            log.warn("Failed to send message to vHost "+status.domain+status.contextRoot, e);
          }
        }
      }
    }

    /**
     * Ping all sites that are not finished deploying and updates the 
     * status according to the ping results.
     * 
     * @param me The current channel member.
     */
    private void pingSites(ChannelMember me) {
      if(!sitePingers.isEmpty()) {
        for(DeploymentStatus statusKey : sitePingers.keySet()) {
          SitePinger pinger = sitePingers.get(statusKey);
          try {
            PingStatus status = pinger.ping();
            if(status == PingStatus.OK) {
              statusKey.setMemberFinished(me);
              statusKey.cancelSitePinger();
            } else if(status == PingStatus.IN_MAINTENANCE) {
              statusKey.logToMember(me, "JBoss deployed site. Cadmium initializing.");
            } else if(status == PingStatus.UNAVAILABLE) {
              statusKey.logToMember(me, "Waiting for JBoss to deploy site.");
            }
          } catch(CadmiumDeploymentException e) {
            statusKey.exception = e;
            statusKey.cancelSitePinger();
          } catch(Exception e) {
            log.warn("Failed to ping site "+statusKey.domain+":"+statusKey.contextRoot, e);
          }
        }
      }
    }
    
  }

  @Override
  public void close() throws IOException {
    scheduler.cancel();
    for(DeploymentStatus status : sitePingers.keySet()) {
      try {
        sitePingers.get(status).close();
      } catch(Throwable t) {
        log.debug("Failed to close pinger for "+status.domain+":"+status.contextRoot, t);
      }
    }
  }
}
