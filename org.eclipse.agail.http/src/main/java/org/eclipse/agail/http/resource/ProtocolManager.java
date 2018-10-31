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

import java.lang.reflect.Method;
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

import org.eclipse.agail.http.service.DbusClient;
import org.eclipse.agail.object.DeviceOverview;
import org.eclipse.agail.object.DiscoveryStatus;
import org.eclipse.agail.object.ProtocolOverview;
import org.eclipse.agail.protocols.config.ProtocolConfig;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Path("/protocols")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProtocolManager {

	protected Logger logger = LoggerFactory.getLogger(ProtocolManager.class);

	@Inject
	DbusClient client;

	protected org.eclipse.agail.ProtocolManager getProtocolManager() throws DBusException {
		return client.getProtocolManager();
	}

	@GET
	@Path("/devices")
	public List<DeviceOverview> Devices() throws DBusException {
		return getProtocolManager().Devices();
	}

	@POST
	@Path("/{id}")
	public void Add(@PathParam("id") String protocol) throws DBusException {
		getProtocolManager().Add(protocol);
	}

	@DELETE
	@Path("/{id}")
	public void Remove(@PathParam("id") String protocol) throws DBusException {
		getProtocolManager().Remove(protocol);
	}

	@GET
	public List<ProtocolOverview> Protocols() throws DBusException {
		return getProtocolManager().Protocols();
	}

	@GET
	@Path("/discovery")
	public List<DiscoveryStatus> DiscoveryStatus() throws DBusException {
		return getProtocolManager().DiscoveryStatus();
	}

	@POST
	@Path("/discovery")
	public void StartDiscovery() throws DBusException {
		getProtocolManager().StartDiscovery();
	}

	@DELETE
	@Path("/discovery")
	public void StopDiscovery() throws DBusException {
		getProtocolManager().StopDiscovery();
	}
	
	@POST
	@Path("/protocolconfig/{id}")
	public void SaveProtocolConfig(@PathParam("id") String protocol, List<ProtocolConfig> protocolConfigs) throws DBusException {
		if(protocolConfigs != null && protocolConfigs.size() > 0) {
//			logger.debug("================HTTP===============");
//			logger.debug("{} Procotol Configurations: {}", protocol, protocolConfigs);
//			try {
//	            Class c = getProtocolManager().getClass();
//	            Method[] m = c.getMethods();
//	            for (int i = 0; i < m.length; i++)
//	            System.out.println("=============================" + m[i].toString());
//	        } catch (Throwable e) {
//	            System.err.println(e);
//	        }
			getProtocolManager().SetProtocolConfigurations(protocol, protocolConfigs);
		}
	}
	  
	@GET
	@Path("/protocolconfig/{id}")
	public List<ProtocolConfig> GetProtocolConfig(@PathParam("id") String protocol) throws DBusException {
		return getProtocolManager().GetProtocolConfigurations(protocol);
	}


}
