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


import java.util.List;
import java.util.Map;

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

import iot.agile.http.service.DbusClient;
import iot.agile.object.DeviceOverview;
import iot.agile.object.RecordObject;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/protocol/{id}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Protocol {
  
  protected Logger logger = LoggerFactory.getLogger(Protocol.class);
  
  @Inject DbusClient client;
  
  protected iot.agile.Protocol getProtocol(String id) throws DBusException {
    return client.getProtocol(id);
  }
  
  @GET
  @Path("/devices")
  public List<DeviceOverview> Devices(@PathParam("id") String id) throws DBusException {
    return getProtocol(id).Devices();
  }
  
  @POST
  @Path("/connection/{deviceId}")
  public void Connect(@PathParam("id") String id, @PathParam("deviceId") String deviceId) throws DBusException {
    getProtocol(id).Connect(deviceId);
  }
  
  @DELETE
  @Path("/connection/{deviceId}")
  public void Disconnect(@PathParam("id") String id, @PathParam("deviceId") String deviceId) throws DBusException {
    getProtocol(id).Disconnect(deviceId);
  }

  @GET
  @Path("/discovery")
  public String DiscoveryStatus(@PathParam("id") String id) throws DBusException {
    return getProtocol(id).DiscoveryStatus();
  }

  @POST
  @Path("/discovery")
  public void Discover(@PathParam("id") String id) throws DBusException {
    getProtocol(id).StartDiscovery();
  }

  @DELETE
  @Path("/discovery")
  public void StopDiscovery(@PathParam("id") String id) throws DBusException {
    getProtocol(id).StopDiscovery();
  }

  @POST  
  @Path("/{deviceAddress}")
  public void Write(
          @PathParam("id") String id, 
          @PathParam("deviceAddress") String deviceAddress, 
          Map<String, String> profile
  ) throws DBusException {
     getProtocol(id).Write(deviceAddress, profile);
  }

  @GET
  @Path("/{deviceAddress}")
  public byte[] Read(
          @PathParam("id") String id,
          @PathParam("deviceAddress") String deviceAddress, 
          Map<String, String> profile
    ) throws DBusException {
    return getProtocol(id).Read(deviceAddress, profile);
  }

  @POST
  @PathParam("/subscribe")
  public void Subscribe(
          @PathParam("id") String id,
          @PathParam("deviceAddress") String deviceAddress, 
          Map<String, String> profile
    ) throws DBusException {
    getProtocol(id).Subscribe(deviceAddress, profile);
  }

}
