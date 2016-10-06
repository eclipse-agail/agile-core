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


import java.util.HashMap;
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

import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iot.agile.http.service.DbusClient;
import iot.agile.object.DeviceComponent;
import iot.agile.object.RecordObject;
import iot.agile.object.StatusType;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/device/{id}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Device {
  
  protected Logger logger = LoggerFactory.getLogger(Device.class);
  
  @Inject DbusClient client;
  
  protected iot.agile.Device getDevice(String id) throws DBusException {
    int pos = id.indexOf('_');
    if (pos < 0) {
      return client.getDevice(id);
    } else {
      return client.getDevice("iot.agile.Device." + id.substring(0, pos), id.substring(pos+1));
    }
  }

  @GET
  @Path("/profile")
  public List<DeviceComponent> Profile(@PathParam("id") String id) throws DBusException {
    return getDevice(id).Profile();
  }
  
  @GET
  @Path("/status")
  public StatusType Status(@PathParam("id") String id) throws DBusException {
    return getDevice(id).Status();
  }

  @GET
  public List<RecordObject> Read(@PathParam("id") String id) throws DBusException {
    return getDevice(id).ReadAll();
  }

  @GET
  @Path("/lastUpdate")
  public List<RecordObject> LastUpdate(@PathParam("id") String id) throws DBusException {
    return getDevice(id).LastUpdateAll();
  }

  @POST
  @Path("/connection")
  public String Connect(@PathParam("id") String id) throws DBusException {
    getDevice(id).Connect();
    return "";
  }

  @DELETE
  @Path("/connection")
  public String Disconnect(@PathParam("id") String id) throws DBusException {
    getDevice(id).Disconnect();
    return "";    
  }

  @POST
  @Path("/execute/{command}")
  public String Execute(@PathParam("id") String id, @PathParam("command") String command, Map<String,Variant> args) throws DBusException {
    if (args == null) {
      args = new HashMap<String,Variant>();
    }
    getDevice(id).Execute(command, args);
    return "";
  }

  @GET
  @Path("/{sensorName}")
  @Produces(MediaType.APPLICATION_JSON)
  public RecordObject Read(@PathParam("id") String id, @PathParam("sensorName") String sensorName) throws DBusException {
    return getDevice(id).Read(sensorName);
  }

  @GET
  @Path("/{componentID}/lastUpdate")
  public RecordObject LastUpdate(@PathParam("id") String id, @PathParam("componentID") String componentID) throws DBusException {
    return getDevice(id).LastUpdate(componentID);
  }


  @POST
  @Path("/{sensorName}")
  public String Write(@PathParam("id") String id, @PathParam("sensorName") String sensorName) throws DBusException {
    getDevice(id).Write();
    return "";
  }

  @POST
  @Path("/{sensorName}/subscribe")
  public String Subscribe(@PathParam("id") String id, @PathParam("sensorName") String sensorName) throws DBusException {
	  getDevice(id).Subscribe(sensorName);
	  return "";
  }
  
  @DELETE
  @Path("/{sensorName}/subscribe")
  public String Unsubscribe(@PathParam("id") String id, @PathParam("sensorName") String sensorName) throws DBusException {
	  getDevice(id).Unsubscribe(sensorName);
	  return "";
  }
  
}
