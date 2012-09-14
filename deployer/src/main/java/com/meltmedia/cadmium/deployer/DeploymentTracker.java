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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    DeploymentStatus keyStatus = getEmptyStatus(domain, contextRoot);
    if(deploymentStatuses.contains(keyStatus)) {
      return deploymentStatuses.get(deploymentStatuses.indexOf(keyStatus));
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
      status = getEmptyStatus(domain, contextRoot);
      deploymentStatuses.add(status);
    } else {
      status.clear();
    }
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
  private DeploymentStatus getEmptyStatus(String domain, String contextRoot) {
    return new DeploymentStatus(domain, contextRoot);
  }
  
  
  /**
   * Status Object for tracking a deployment.
   * 
   * @author John McEntire
   *
   */
  public class DeploymentStatus {
    CadmiumDeploymentException exception;
    boolean waiting = false;
    boolean deployed = false;
    Map<String, List<String>> memberLogs = new Hashtable<String, List<String>>();
    Map<String, Boolean> memberStatus = new Hashtable<String, Boolean>();
    String domain;
    String contextRoot;
    
    /**
     * Creates a new instance with the required fields.
     * @param domain 
     * @param contextRoot
     */
    public DeploymentStatus(String domain, String contextRoot) {
      this.domain = domain;
      this.contextRoot = contextRoot;
    }
    
    /**
     * @param member
     * @return The List of current deployment log messages for the given member.
     * Adds a new list and returns that in the case that the member has no list yet.
     */
    public List<String> getMemberLogs(ChannelMember member) {
      synchronized(this) {
        String key= getKey(member);
        if(!memberLogs.containsKey(key)) {
          memberLogs.put(key, new ArrayList<String>());
        }
        return memberLogs.get(key);
      }
    }
    
    /**
     * Adds a message to a members logs if the last log was not the same log.
     * 
     * @param member
     * @param message
     */
    public void logToMember(ChannelMember member, String message) {
      synchronized(this) {
        String key= getKey(member);
        if(!memberLogs.containsKey(key)) {
          memberLogs.put(key, new ArrayList<String>());
        }
        List<String> logs = memberLogs.get(key);
        if(logs.isEmpty() || logs.indexOf(message) != logs.size() - 1){
          logs.add(message);
        }
      }
    }
    
    /**
     * 
     * @param member
     * @return The current deployment status of the given member. Adds a false for the 
     * given member and returns that in the case that the member has no status yet.
     */
    public Boolean getMemberFinished(ChannelMember member) {
      synchronized(this) {
        String key= getKey(member);
        if(memberStatus.containsKey(key)) {
          return memberStatus.get(key);
        }
        return false;
      }
    }
    
    /**
     * Sets the member to finished.
     * 
     * @param member
     */
    public void setMemberFinished(ChannelMember member) {
      synchronized(this) {
        String key= getKey(member);
        memberStatus.put(key, true);
      }
    }
    
    /**
     * Sets the member to not finished.
     * 
     * @param member
     */
    public void setMemberStarted(ChannelMember member) {
      synchronized(this) {
        String key= getKey(member);
        memberStatus.put(key, false);
      }
    }
    
    /**
     * Creates a String key from a {@link ChannelMember}.
     * @param member
     * @return
     */
    private String getKey(ChannelMember member) {
      return member.toString();
    }
  
    /**
     * @return True if all members are finished.
     */
    public boolean isFinished() {
      for(String mem : memberStatus.keySet()) {
        Boolean memFinished = memberStatus.get(mem);
        if(!memFinished) {
          return false;
        }
      }
      return !memberStatus.isEmpty();
    }
    
    /**
     * Clears previously set values for all fields except the domain and contextRoot.
     */
    public void clear() {
      synchronized(this) {
        exception = null;
        waiting = false;
        memberLogs.clear();
        memberStatus.clear();
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
    
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
          + ((contextRoot == null) ? 0 : contextRoot.hashCode());
      result = prime * result + ((domain == null) ? 0 : domain.hashCode());
      return result;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      DeploymentStatus other = (DeploymentStatus) obj;
      if (contextRoot == null) {
        if (other.contextRoot != null)
          return false;
      } else if (!contextRoot.equals(other.contextRoot))
        return false;
      if (domain == null) {
        if (other.domain != null)
          return false;
      } else if (!domain.equals(other.domain))
        return false;
      return true;
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
              status.logToMember(me, "JBoss Deployment State changed to: "+deployState);
              if(deployState == DeploymentState.DEPLOYED) {
                status.deployed = true;
                startPinging(status.domain, status.contextRoot);
              }
            } catch(CadmiumDeploymentException e) {
              status.cancelSitePinger();
              status.exception = e;
            } catch(Exception e) {
              status.cancelSitePinger();
              status.exception = new CadmiumDeploymentException("Failed to deploy.",e);
              log.warn("Failed to deploy", e);
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
            try {
              JBossUtil.forceDeleteCadmiumWar(log, statusKey.domain + "war");
            } catch (IOException e1) {
              log.warn("Failed to undeploy war for domain "+statusKey.domain+":"+statusKey.contextRoot, e1);
            }
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
