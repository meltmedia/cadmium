package com.meltmedia.cadmium.servlets;

import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RedirectFilterTest {

    private String[] inputRequests = {
        "http://domain.com/some/path/with?params=true",
        "http://domain.com/some/path/without/",
        "http://domain.com/some/path?extra=1"
    };

    private String[] expectedResponses = {
        "http://domain.com/redirected/path?params=true",
        "http://domain.com/redirected/path/with?params=true",
        "http://domain.com/redirected/path/with?params=true&extra=1"
    };

    @Test
    public void testDoFilter() throws Exception {
        RedirectFilter filter = new RedirectFilter();
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream out = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(out);
        FilterChain chain = mock(FilterChain.class);


        //rinse & repeat this for each input/response pair
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL().thenReturn(new StringBuffer(inputRequests[0])));

        filter.doFilter(request, response,chain);

        verify(response).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        verify(response).setHeader("Location", expectedResponses[0]);
    }
}