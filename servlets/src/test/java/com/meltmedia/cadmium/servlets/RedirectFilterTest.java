package com.meltmedia.cadmium.servlets;

import org.junit.Test;


public class RedirectFilterTest {

    @Test
    public void testDoFilter() throws Exception {

        //If path = http://domain.com/some/path?params=true
        //  redir should = http://domain.com/redirected/path?params=true

        //If path = http://domain.com/some/path/without/params
        //  redir should = http://domain.com/redirected/path/with?params=true

        //If path = http://domain.com/some/path?extra=1
        //  redir should = http://domain.com/redirected/path/with?params=true&extra=1
    }
}