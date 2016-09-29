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
    };

    private String[] basicRedirectTest = {"/path/to/redirected",null,"/redirected"};
    private String[] redirectWithSourceParamsTest = {"/path/to/redirected","params=true","/redirected?params=true"};
    private String[] redirectWithTargetParamsTest = {"/path/to/params",null,"/redirected/with?params=true"};
    private String[] redirectWithSourceAndTargetParamsTest = {"/path/to/params","zero=100","/redirected/with?params=true&zero=100"};

    @Test
    public void testBasicRedirect() throws Exception {
        runRedirectFilterTest(basicRedirectTest,basicRedirectTest[2]);
    }

    @Test
    public void testRedirectWithSourceParams() throws Exception {
        runRedirectFilterTest(redirectWithSourceParamsTest,redirectWithSourceParamsTest[2]);
    }

    @Test
    public void testRedirectWithTargetParamsTest() throws Exception {
        runRedirectFilterTest(redirectWithTargetParamsTest,redirectWithTargetParamsTest[2]);
    }

    @Test
    public void testRedirectWithSourceAndTargetParamsTest() throws Exception {
        runRedirectFilterTest(redirectWithSourceAndTargetParamsTest,redirectWithSourceAndTargetParamsTest[2]);
    }

    public void runRedirectFilterTest(String[] testParams, String expectedResult) throws Exception {
        RedirectFilter filter = new RedirectFilter();
        HttpServletResponse response = mock(HttpServletResponse.class);

        FilterChain chain = mock(FilterChain.class);
        RedirectConfigProcessor redirectConfigProcessor = mock(RedirectConfigProcessor.class);
        Redirect redirect = mock(Redirect.class);
        filter.redirect = redirectConfigProcessor;
        when(filter.redirect.requestMatches(anyString(),anyString())).thenReturn(redirect);

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(redirect.getUrlSubstituted()).thenReturn(getRedirectDestinationFromPath(testParams[0]));
        when(request.getRequestURI()).thenReturn(testParams[0]);
        when(request.getQueryString()).thenReturn(testParams[1]);
        filter.doFilter(request, response,chain);

        verify(response).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        verify(response).setHeader("Location", expectedResult);
    }

    private String getRedirectDestinationFromPath(String path){
        for(int i = 0;i < pathRedirectPairs.length;i++){
            if(pathRedirectPairs[i][0].equals(path)){
                return pathRedirectPairs[i][1];
            }
        }

        return path;
    }
}