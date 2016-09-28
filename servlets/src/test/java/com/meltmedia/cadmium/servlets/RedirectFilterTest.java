package com.meltmedia.cadmium.servlets;

import com.meltmedia.cadmium.core.meta.Redirect;
import com.meltmedia.cadmium.core.meta.RedirectConfigProcessor;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    //Each array is broken into requestPath, requestQuery, expectedResponse
    private String[][] testGroups = new String[][] {
        {"/path/to/redirected",null,"/redirected"},
        {"/path/to/redirected","params=true","/redirected?params=true"},
        {"/path/to/params",null,"/redirected/with?params=true"},
        {"/path/to/params","zero=100","/redirected/with?params=true&zero=100"},
        {"/anypath/anything",null,"/redirected/regex/path"},
        {"/anypath/anything","params=true","/redirected/regex/path?params=true"}
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

        HttpServletRequest request = mock(HttpServletRequest.class);


        for(int i = 0; i < testGroups.length;i++){
            String requestPath = testGroups[i][0];
            String requestQuery = testGroups[i][1];
            String expectedResponse = testGroups[i][2];

            when(redirect.getUrlSubstituted()).thenReturn(getRedirectDestinationFromPath(requestPath));
            when(request.getRequestURI()).thenReturn(requestPath);
            when(request.getQueryString()).thenReturn(requestQuery);

            filter.doFilter(request, response,chain);

            verify(response).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            verify(response).setHeader("Location", expectedResponse);
        }
    }

    private String getRedirectDestinationFromPath(String path){
        for(int i = 0;i < pathRedirectPairs.length;i++){
            if(pathRedirectPairs[i][0] == path){
                return pathRedirectPairs[i][1];
            }
        }

        return path;
    }
}