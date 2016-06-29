package com.meltmedia.cadmium.servlets.jersey;

import com.meltmedia.cadmium.servlets.jersey.error.ErrorUtil;
import com.meltmedia.cadmium.servlets.jersey.error.GenericError;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Test for {@link com.meltmedia.cadmium.servlets.jersey.error.ErrorUtil}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { GenericError.class, System.class })
public class ErrorUtilTest {

  UriInfo uriInfo = mock(UriInfo.class);

  @Before
  public void setup() throws URISyntaxException {
    when(uriInfo.getAbsolutePath()).thenReturn(new URI("/mockpath"));
  }



  @Test
  public void shouldReturnFalseWhenEnvironmentIsProduction() {
    assertThat("Environment should not be production", ErrorUtil.isProduction(), equalTo(false));
  }

  @Test
  public void shouldReturnTrueWhenEnvironmentIsProduction() {
    mockStatic(System.class);

    when(System.getProperty(Mockito.anyString())).thenReturn("production");
    assertThat("Environment should not be production", ErrorUtil.isProduction(), equalTo(false));
  }

  @Test
  public void shouldReturnErrorResponseForGivenException() throws URISyntaxException {

    //When: I invoke internalError
    Response response = ErrorUtil.internalError(new Exception("Mock"), uriInfo);

    //Then: Expected response is returned
    final GenericError error = (GenericError) response.getEntity();
    assertThat("Status code is set to 500", response.getStatus(),
        equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    assertThat("Expected error message is set", error.getMessage(), equalTo("Exception: Mock"));
    assertThat("Expected path is set", error.getPath(), equalTo("/mockpath"));
    assertThat("Expected code is set", error.getCode(), equalTo(ErrorUtil.ErrorCode.INTERNAL.getCode()));

  }

  @Test
  public void shouldThrowWebApplicationExceptionWhenThereIsInternalErrorDuringProcessing() throws URISyntaxException {

    //When: I invoke internalError
    WebApplicationException waex = null;
    try {
      ErrorUtil.throwInternalError("MockError", uriInfo);
    } catch (WebApplicationException exc) {
      waex = exc;
    }

    //Then: Expected response is returned
    assertThat("WebApplicationException was thrown", waex, CoreMatchers.<WebApplicationException>notNullValue());
    final Response response = waex.getResponse();
    final GenericError error = (GenericError) response.getEntity();
    assertThat("Status code is set to 500", response.getStatus(),
        equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    assertThat("Expected error message is set", error.getMessage(), equalTo("MockError"));
    assertThat("Expected path is set", error.getPath(), equalTo("/mockpath"));
    assertThat("Expected code is set", error.getCode(), equalTo(ErrorUtil.ErrorCode.INTERNAL.getCode()));

  }

}
