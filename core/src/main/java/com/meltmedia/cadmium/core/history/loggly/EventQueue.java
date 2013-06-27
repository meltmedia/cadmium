package com.meltmedia.cadmium.core.history.loggly;

import com.meltmedia.cadmium.core.Scheduled;
import com.meltmedia.cadmium.core.messaging.MembershipTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Queue for unsent events.
 *
 * @author jmcentire
 */
@Singleton
public class EventQueue {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Inject
  protected Api logglyService;

  @Inject
  protected MembershipTracker membershipTracker;

  private BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>();

  public void log(Event evt) {
    if(membershipTracker.getCoordinator().isMine()) {
      if(eventQueue.offer(evt)){
        logger.debug("Added {} to the event queue.", evt);
      } else {
        logger.warn("Failed to add {} to the event queue.", evt);
      }
    }
  }

  @Scheduled(delay = 5l, interval = 5l, unit = TimeUnit.MINUTES)
  public void dequeueLogs() {
    Event evt = null;
    logger.debug("Dequeueing Logs.");
    do {
      try {
        evt = eventQueue.poll(10l, TimeUnit.SECONDS);
        if(evt != null) {
          logglyService.sendEvent(evt);
        }
      } catch(Throwable t) {
        logger.error("Failed to send event "+evt, t);
        break;
      }
    } while(evt != null);
  }

}
