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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.freedesktop.DBus.Error.UnknownObject;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iot.agile.exception.AgileDeviceNotFoundException;
import iot.agile.exception.AgileNoResultException;
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
    try {
      return getDevice(id).Profile();
    } catch (AgileNoResultException e) {
      return null;
    }catch (UnknownObject ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception ex) {
      throw new WebApplicationException("Error on reading profile", ex);
    } 
   }
  
  @GET
  @Path("/status")
  public StatusType Status(@PathParam("id") String id) throws DBusException {
    try {
      return getDevice(id).Status();
    } catch (UnknownObject ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception ex) {
      throw new WebApplicationException("Error on reading status", ex);
    }
  }

  @GET
  @Path("/lastSeen")
  public long LastSeen(@PathParam("id") String id) throws DBusException {
     try {
      return getDevice(id).LastSeen();
    } catch (UnknownObject ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    }  catch (AgileNoResultException e) {
      throw e;
    }catch (Exception ex) {
      throw new WebApplicationException("Error on reading data", ex);
    }
  }

	@GET
	public List<RecordObject> Read(@PathParam("id") String id) throws DBusException {
    List<RecordObject> result = null;
    try {
      result = getDevice(id).ReadAll();
    } catch (AgileNoResultException e) {
      return null;
    } catch (UnknownObject ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception ex) {
      throw new WebApplicationException("Error on reading data", ex);
    }
    if (result.size() == 0) {
      return null;
    }
    return result;
	}

	@GET
	@Path("/lastUpdate")
	public List<RecordObject> LastUpdate(@PathParam("id") String id) throws DBusException {
    List<RecordObject> result = null;
    try {
      result = getDevice(id).LastUpdateAll();
    } catch (UnknownObject ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (AgileNoResultException e) {
      return null;
    } catch (Exception ex) {
      throw new WebApplicationException("Error on reading data", ex);
    }
    if (result.size() == 0) {
      return null;
    }
    return result;
	}

  @POST
  @Path("/connection")
  public void Connect(@PathParam("id") String id) throws DBusException {
    try {
      getDevice(id).Connect();
    } catch (UnknownObject ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception e) {
      throw new WebApplicationException("Error on connecting device", e);
    }
   }

  @DELETE
  @Path("/connection")
  public void Disconnect(@PathParam("id") String id) throws DBusException {
    try {
      getDevice(id).Disconnect();
    } catch (UnknownObject ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception e) {
      throw new WebApplicationException("Error on disconnecting device", e);
    }  
   }

  @POST
  @Path("/execute/{command}")
  public void Execute(@PathParam("id") String id, @PathParam("command") String command, Map<String,Variant> args) 
      throws DBusException {
    if (args == null) {
      args = new HashMap<String,Variant>();
    }
    try {
      getDevice(id).Execute(command, args);
    } catch (UnknownObject ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception e) {
      throw new WebApplicationException("Error on executing command", e);
    }  
   }

	@GET
	@Path("/{sensorName}")
	@Produces(MediaType.APPLICATION_JSON)
	public RecordObject Read(@PathParam("id") String id, @PathParam("sensorName") String sensorName)
			throws DBusException {
    try {
      return getDevice(id).Read(sensorName);
    } catch (AgileNoResultException e) {
      return null;
    } catch (UnknownObject ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception ex) {
      throw new WebApplicationException("Error on reading data", ex);
    }
	}

	@GET
	@Path("/{componentID}/lastUpdate")
	public RecordObject LastUpdate(@PathParam("id") String id, @PathParam("componentID") String componentID)
			throws DBusException {
    try {
      return getDevice(id).LastUpdate(componentID);
    } catch (AgileNoResultException e) {
      return null;
    } catch (UnknownObject ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception ex) {
      throw new WebApplicationException("Error on reading data", ex);
    }
	}

  @POST
  @Path("/{sensorName}")
  public void Write(@PathParam("id") String id, @PathParam("sensorName") String sensorName) throws DBusException {
    try {
      getDevice(id).Write();
    } catch (UnknownObject ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception ex) {
      throw new WebApplicationException("Error on writing data", ex);
    }
  }

  @POST
  @Path("/{sensorName}/subscribe")
  public void Subscribe(@PathParam("id") String id, @PathParam("sensorName") String sensorName) throws DBusException {
    try {
      getDevice(id).Subscribe(sensorName);
    } catch (UnknownObject ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception ex) {
      throw new WebApplicationException("Error on subscribing for data", ex);
    }
  }
  
  @DELETE
  @Path("/{sensorName}/subscribe")
  public void Unsubscribe(@PathParam("id") String id, @PathParam("sensorName") String sensorName) throws DBusException {
    try {
      getDevice(id).Unsubscribe(sensorName);
    } catch (UnknownObject ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception ex) {
      throw new WebApplicationException("Error on unsubscribing for data", ex);
    }
  }
}
