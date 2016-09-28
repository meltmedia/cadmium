package com.meltmedia.cadmium.servlets;

import com.meltmedia.cadmium.core.meta.Redirect;
import com.meltmedia.cadmium.core.meta.RedirectConfigProcessor;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RedirectFilterTest {

    private String[][] pathRedirectPairs = new String[][] {
        {"/path/to/redirected","/redirected"},
        {"/path/to/params", "/redirected/with?params=true"},
        {"/anypath/*", "/redirected/regex/path"}
    };

    private String[][] inputResponsePairs = new String[][] {
        {"/path/to/redirected",null,"/redirected"},
        {"/path/to/redirected","params=true","/redirected?params=true"},
        {"/path/to/params",null,"/redirected/with?params=true"},
        {"/path/to/params","zero=100","/redirected/with?params=true&zero=100"},
        {"/anypath/*",null,"/redirected/regex/path"},
        {"/anypath/*","params=true","/redirected/regex/path?params=true"}
    };

    @Test
    public void testDoFilter() throws Exception {
        RedirectFilter filter = new RedirectFilter();
        HttpServletResponse response = mock(HttpServletResponse.class);

        FilterChain chain = mock(FilterChain.class);
        RedirectConfigProcessor redirectConfigProcessor = mock(RedirectConfigProcessor.class);
        Redirect redirect = mock(Redirect.class);
        filter.redirect = redirectConfigProcessor;
        when(filter.redirect.requestMatches(anyString(),anyString())).thenReturn(redirect);
        when(redirect.getUrlSubstituted()).thenReturn(pathRedirectPairs[0][1]);

        //rinse & repeat this for each input/response pair
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(inputResponsePairs[1][0]);//Gets path
        when(request.getQueryString()).thenReturn(inputResponsePairs[1][1]);//Gets Query

        filter.doFilter(request, response,chain);

        verify(response).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        verify(response).setHeader("Location", inputResponsePairs[1][2]);
    }
}