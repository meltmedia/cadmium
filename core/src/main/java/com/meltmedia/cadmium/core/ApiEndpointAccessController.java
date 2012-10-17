package com.meltmedia.cadmium.core;

/**
 * The interface that the api access controller must implement.
 * 
 * @author John McEntire
 *
 */
public interface ApiEndpointAccessController {
  /**
   * Reenables the passed in endpoint. Do not include the path prefix that the jersey service is bound to.
   * @param endpoint
   */
  public void enable(String endpoint);
  /**
   * Disables the passed in endpoint. Do not include the path prefix that the jersey service is bound to.
   * @param endpoint
   */
  public void disable(String endpoint);
  /**
   * Lists disabled endpoints. Does not include the path prefix that the jersey service is bound to.
   * @param endpoint
   */
  public String[] getDisabled();
}
