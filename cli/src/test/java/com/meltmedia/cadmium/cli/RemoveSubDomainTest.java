/**
 *    Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.cli;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RemoveSubDomainTest {
  
  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { "http://sub.domain.com/", "http://domain.com/" },
        { "http://sub.sub.domain.com/", "http://sub.domain.com/" },
        { "http://sub.sub.domain.com:8080/", "http://sub.domain.com:8080/" }});
  }
  private String expected;
  private String url;

  public RemoveSubDomainTest( String url, String expected ) {
    this.url = url;
    this.expected = expected;
  }
  
  @Test
  public void test() {
    String actual = DeployCommand.removeSubDomain(url);
    
    assertEquals("Subdomain was not properly removed.", expected, actual);
  }
}
