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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iot.agile.http.Util;
import iot.agile.http.resource.devicemanager.BatchBody;
import iot.agile.http.service.DbusClient;
import iot.agile.object.DeviceDefinition;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/devices")
public class DeviceManager {
  
  protected Logger logger = LoggerFactory.getLogger(DeviceManager.class);
  
  @Inject DbusClient client;
  
  ObjectMapper mapper = Util.mapper;
  
  @POST
//  public String Create(@NotNull DeviceDefinition body) throws DBusException {
  public Map<String,String> Create(DeviceDefinition body) throws DBusException, IOException {
    logger.debug("Create new device {} ({}) on {}", body.id, body.name, body.protocol);
    return client.getDeviceManager().Create(body);
  }
  
  @GET
  public List<Map<String, String>> List() throws DBusException, JsonProcessingException {
    return client.getDeviceManager().devices();
  }
  
  @POST
  @Path("/find")
  public String Find() {
    throw new InternalError("Not implemented");
  }

  @GET
  @Path("/{id}")
  public String Read(@PathParam("id") String id) {
//    return client.getDeviceManager().Read(id);
    throw new InternalError("Not implemented");
  }

  @PUT
  @Path("/{id}")
  public boolean Update(@PathParam("id") String id, String definition) throws DBusException {
    return client.getDeviceManager().Update(id, definition);
  }

  @DELETE
  @Path("/{id}")
  public void Delete(@PathParam("id") String id, String definition) throws DBusException {
    client.getDeviceManager().Delete(id, definition);
  }

  @POST
  @Path("/batch")
  public String Batch(BatchBody body) throws DBusException {
    client.getDeviceManager().Batch(body.operation, body.arguments);
    return "";
  }
  
}
