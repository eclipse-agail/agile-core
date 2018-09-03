/*******************************************************************************
 * Copyright (C) 2017 Create-Net / FBK.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Create-Net / FBK - initial API and implementation
 ******************************************************************************/

package org.eclipse.agail.http.resource;

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
import org.freedesktop.DBus.Error.ServiceUnknown;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.agail.exception.AgileDeviceNotFoundException;
import org.eclipse.agail.exception.AgileNoResultException;
import org.eclipse.agail.http.service.DbusClient;
import org.eclipse.agail.object.DeviceComponent;
import org.eclipse.agail.object.RecordObject;
import org.eclipse.agail.object.StatusType;

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
  
  protected org.eclipse.agail.Device getDevice(String id) throws DBusException {
    int pos = id.indexOf('_');
    if (pos < 0) {
      return client.getDevice(id);
    } else {
      return client.getDevice("org.eclipse.agail.Device." + id.substring(0, pos), id.substring(pos+1));
    }
  }

  @GET
  @Path("/profile")
  public List<DeviceComponent> Profile(@PathParam("id") String id) throws DBusException {
    try {
      return getDevice(id).Profile();
    } catch (AgileNoResultException e) {
      return null;
    }catch (UnknownObject | ServiceUnknown ex) {
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
    } catch (UnknownObject | ServiceUnknown ex) {
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
    } catch (UnknownObject | ServiceUnknown ex) {
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
    } catch (UnknownObject | ServiceUnknown ex) {
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
    } catch (UnknownObject | ServiceUnknown ex) {
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
      logger.debug("Connect on {}", id);
      getDevice(id).Connect();
    } catch (UnknownObject | ServiceUnknown ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception e) {
      throw new WebApplicationException("Error on connecting device", e);
    }
   }

  @DELETE
  @Path("/connection")
  public void Disconnect(@PathParam("id") String id) throws DBusException {
    try {
      logger.debug("Disconnect on {}", id);
      getDevice(id).Disconnect();
    } catch (UnknownObject | ServiceUnknown ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception e) {
      throw new WebApplicationException("Error on disconnecting device", e);
    }  
   }

  @POST
  @Path("/execute/{commandId}")
  public void Execute(@PathParam("id") String id, @PathParam("commandId") String commandId) 
      throws DBusException {
    try {
      getDevice(id).Execute(commandId);
    } catch (UnknownObject | ServiceUnknown ex) {
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
      logger.debug("Read on {}/{}", id, sensorName);
      return getDevice(id).Read(sensorName);
    } catch (AgileNoResultException e) {
      return null;
    } catch (UnknownObject | ServiceUnknown ex) {
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
      logger.debug("LastUpdate on {}/{}", id, componentID);
      return getDevice(id).LastUpdate(componentID);
    } catch (AgileNoResultException e) {
      return null;
    } catch (UnknownObject | ServiceUnknown ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception ex) {
      throw new WebApplicationException("Error on reading data", ex);
    }
	}

  @POST
  @Path("/{componentName}/{payload}")
  public void Write(@PathParam("id") String id, @PathParam("componentName") String componentName, @PathParam("payload") String payload) throws DBusException {
    try {
      getDevice(id).Write(componentName, payload);
    } catch (UnknownObject | ServiceUnknown ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception ex) {
      throw new WebApplicationException("Error on writing data", ex);
    }
  }

  @POST
  @Path("/{sensorName}/subscribe")
  public void Subscribe(@PathParam("id") String id, @PathParam("sensorName") String sensorName) throws DBusException {
    try {
      logger.debug("Subscribe to {}/{}", id, sensorName);
      getDevice(id).Subscribe(sensorName);
    } catch (UnknownObject | ServiceUnknown ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception ex) {
      throw new WebApplicationException("Error on subscribing for data", ex);
    }
  }
  
  @DELETE
  @Path("/{sensorName}/subscribe")
  public void Unsubscribe(@PathParam("id") String id, @PathParam("sensorName") String sensorName) throws DBusException {
    try {
      logger.debug("Unsubscribe from {}/{}", id, sensorName);
      getDevice(id).Unsubscribe(sensorName);
    } catch (UnknownObject | ServiceUnknown ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception ex) {
      throw new WebApplicationException("Error on unsubscribing for data", ex);
    }
  }
  
    @GET
  @Path("/commands")
  public List<String> Commands(@PathParam("id") String id) throws DBusException {
    try {
      return getDevice(id).Commands();
    } catch (UnknownObject | ServiceUnknown ex) {
      throw new AgileDeviceNotFoundException("Device not found");
    } catch (Exception ex) {
      throw new WebApplicationException("Error on unsubscribing for data", ex);
    }
  }
}
