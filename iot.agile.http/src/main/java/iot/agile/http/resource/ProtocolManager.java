/*
 * Copyright 2016 CREATE-NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package iot.agile.http.resource;


import iot.agile.http.service.DbusClient;
import iot.agile.object.DeviceOverview;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/protocols")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProtocolManager {
  
  protected Logger logger = LoggerFactory.getLogger(ProtocolManager.class);
  
  @Inject DbusClient client;
  
  protected iot.agile.ProtocolManager getProtocolManager() throws DBusException {
    return client.getProtocolManager();
  }
    
  @GET
  @Path("/devices")
  public List<DeviceOverview> Devices() throws DBusException {
    return getProtocolManager().Devices();
  }

  @POST
  @Path("/{id}")
  public String Add(@PathParam("id") String protocol) throws DBusException {
    getProtocolManager().Add(protocol);
    return "";
  }

  @DELETE
  @Path("/{id}")
  public String Remove(@PathParam("id") String protocol) throws DBusException {
    getProtocolManager().Remove(protocol);
    return "";
  }  
  
  @GET
  public List<String> Protocols() throws DBusException {
    return getProtocolManager().Protocols();
  }

  @POST
  @Path("/discovery")
  public String StartDiscovery() throws DBusException {
    getProtocolManager().Discover();
    return "";
  }
  
  @DELETE
  @Path("/discovery")
  public String StopDiscovery() throws DBusException {
    throw new InternalError("Not Implemented");
//    return "";
  }

  
}
