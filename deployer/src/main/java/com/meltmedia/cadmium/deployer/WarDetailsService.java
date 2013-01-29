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
package com.meltmedia.cadmium.deployer;

import java.io.File;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.core.CadmiumSystemEndpoint;
import com.meltmedia.cadmium.core.WarInfo;
import com.meltmedia.cadmium.core.util.WarUtils;
import com.meltmedia.cadmium.servlets.jersey.AuthorizationService;

@CadmiumSystemEndpoint
@Path("/deployment/details")
public class WarDetailsService extends AuthorizationService {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @GET
  @Path("{war}")
  @Produces(MediaType.APPLICATION_JSON)
  public WarInfo getWarInfo(@PathParam("war") String war, @HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    
    File warFile = new File(JBossUtil.getDeployDirectory(log), war);
    
    return WarUtils.getWarInfo(warFile);
  }
}
