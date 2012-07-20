package com.meltmedia.cadmium.email;

import java.util.Collection;

public interface MailSessionManager {
  public EmailService getService() throws EmailException;
  public Collection<EmailService> getServices();
}
