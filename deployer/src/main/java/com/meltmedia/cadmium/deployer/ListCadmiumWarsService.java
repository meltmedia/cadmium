package com.meltmedia.cadmium.deployer;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meltmedia.cadmium.servlets.jersey.AuthorizationService;

@Path("/deployment/list")
public class ListCadmiumWarsService extends AuthorizationService {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> list(@HeaderParam("Authorization") @DefaultValue("no token") String auth) throws Exception {
    if(!this.isAuth(auth)) {
      throw new Exception("Unauthorized!");
    }
    List<String> deployedWars = JBossUtil.listDeployedWars(logger);
    logger.debug("Jboss deployed cadmium wars: {}", Arrays.toString(deployedWars.toArray()));
    return deployedWars;
  }
}
