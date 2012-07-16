package com.meltmedia.cadmium.core.messaging;

public final class ProtocolMessage {
  private ProtocolMessage() {}

  public static String UPDATE = "UPDATE";
  public static String UPDATE_DONE = "UPDATE_DONE";
  public static String UPDATE_FAILED = "UPDATE_FAILED";
  public static String CURRENT_STATE = "CURRENT_STATE";
  public static String STATE_UPDATE = "STATE_UPDATE";
  public static String SYNC = "SYNC";
  public static String HISTORY_REQUEST = "HISTORY_REQUEST";
  public static String HISTORY_RESPONSE = "HISTORY_RESPONSE";
  public static String MAINTENANCE = "MAINTENANCE";
}
