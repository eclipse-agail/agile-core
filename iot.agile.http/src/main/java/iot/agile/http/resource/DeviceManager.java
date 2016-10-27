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

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import iot.agile.http.Util;
import iot.agile.http.resource.devicemanager.BatchBody;
import iot.agile.http.service.DbusClient;
import iot.agile.object.DeviceDefinition;
import iot.agile.object.DeviceOverview;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/devices")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DeviceManager {

	protected Logger logger = LoggerFactory.getLogger(DeviceManager.class);

	@Inject
	DbusClient client;

	ObjectMapper mapper = Util.mapper;

	@Context
	private HttpServletResponse response;

	@POST
	public DeviceDefinition Create(DeviceDefinition body) throws DBusException, IOException {
		logger.debug("Create new device {} ({}) on {}", body.address, body.name, body.protocol);
		return client.getDeviceManager().Create(body);
	}

	@GET
	@Path("/typeof")
	public List<String> MatchingDeviceTypesToDeprecate(DeviceOverview overview) throws DBusException, IOException {
		return client.getDeviceManager().MatchingDeviceTypes(overview);
	}

	@POST
	@Path("/typeof")
	public List<String> MatchingDeviceTypes(DeviceOverview overview) throws DBusException, IOException {
		return client.getDeviceManager().MatchingDeviceTypes(overview);
	}

	public static class RegisterPayload {
		@JsonProperty("overview")
		public DeviceOverview overview;
		@JsonProperty("type")
		public String type;

		@JsonCreator
		public RegisterPayload(@JsonProperty("overview") DeviceOverview overview, @JsonProperty("type") String type){
			this.overview = overview; this.type = type;
		}
	}

	@POST
	@Path("/register")
	public DeviceDefinition Register(RegisterPayload args) throws DBusException, IOException {
		logger.debug("Register new device of type {}: {} ({}) on {}", args.type, args.overview.id, args.overview.name, args.overview.protocol);
		return client.getDeviceManager().Register(args.overview, args.type);
	}

	@GET
	public List<DeviceDefinition> Devices() throws DBusException, JsonProcessingException {
		return client.getDeviceManager().Devices();
	}

	@POST
	@Path("/find")
	public String Find() {
		throw new InternalError("Not implemented");
	}

	@GET
	@Path("/{id}")
	public DeviceDefinition Read(@PathParam("id") String id) throws DBusException {
		try {
			DeviceDefinition result = client.getDeviceManager().Read(id);
			return result;
		} catch (Exception e) {
			logger.debug("device not found");
			ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
			builder.entity("404: Device not found");
			Response response = builder.build();
			throw new WebApplicationException(response);
		}
	}
	
	
	

	@PUT
	@Path("/{id}")
	public void Update(@PathParam("id") String id, DeviceDefinition definition) throws DBusException {
		// TODO: check consistency of id and definition.get("id);
		client.getDeviceManager().Update(id, definition);
	}

	@DELETE
	@Path("/{id}")
	public void Delete(@PathParam("id") String id) throws DBusException {
		try {
			client.getDeviceManager().Delete(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@POST
	@Path("/batch")
	public void Batch(BatchBody body) throws DBusException {
		client.getDeviceManager().Batch(body.operation, body.arguments);
	}

}
