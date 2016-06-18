package com.meltmedia.cadmium.servlets.jersey.error;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Utility class for creating error response.
 */
public class ErrorUtil {

  /**
   * Enum representing different application error codes.
   */
  public enum ErrorCode {
    INTERNAL("INTERNAL");
    String code;
    ErrorCode(String code) {
      this.code=code;
    }
    public String getCode() {
      return code;
    }
  }

  /**
   * Gets the cadmium environment
   * @return <code>String</code> representing cadmium environment
   */
  public static String getEnvironment() {
    return System.getProperty("com.meltmedia.cadmium.environment", "local");
  }

  /**
   * Checks if given cadmium environment is production
   * @return
   */
  public static boolean isProduction() {
    return getEnvironment().toLowerCase().contains("prod");
  }

  /**
   * Creates Jersey response corresponding to internal error
   * @param throwable {@link Throwable} object representing an error
   * @param uriInfo {@link UriInfo} object used for forming links
   * @return {@link Response} Jersey response object containing JSON representation of the error.
   */
  public static Response internalError(Throwable throwable, UriInfo uriInfo) {
    GenericError error =  new GenericError(
        ExceptionUtils.getRootCauseMessage(throwable),
        ErrorCode.INTERNAL.getCode(),
        uriInfo.getAbsolutePath().toString());

    if (!isProduction()) {
      error.setStack(ExceptionUtils.getStackTrace(throwable));
    }

    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity(error)
        .build();
  }

  /**
   * Wraps the error as {@link WebApplicationException} with error mapped as JSON Response
   * @param message {@link String} representing internal error
   * @param uriInfo {@link UriInfo} used for forming link
   */
  public static void throwInternalError(String message, UriInfo uriInfo) {
    GenericError error =  new GenericError(
        message,
        ErrorCode.INTERNAL.getCode(),
        uriInfo.getAbsolutePath().toString());

    throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity(error)
        .build());
  }
}
