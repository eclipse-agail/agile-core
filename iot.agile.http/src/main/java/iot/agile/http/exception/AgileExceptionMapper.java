/*******************************************************************************
 * Copyright (C) 2017 Create-Net / FBK.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Create-Net / FBK - initial API and implementation
 ******************************************************************************/
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
package iot.agile.http.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iot.agile.exception.AgileDeviceNotFoundException;
import iot.agile.exception.AgileNoResultException;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class AgileExceptionMapper implements ExceptionMapper<Throwable> {

	Logger logger = LoggerFactory.getLogger(AgileExceptionMapper.class);

	protected class ErrorMessage {

		public Response.Status code = Response.Status.INTERNAL_SERVER_ERROR;
		public String message;

		public ErrorMessage(Throwable ex) {
			this.message = ex.getMessage();
		}

		public ErrorMessage(String message) {
			this.message = message;
		}

		public ErrorMessage(String message, Response.Status code) {
			this.code = code;
			this.message = message;
		}

	}

	@Override
	public Response toResponse(Throwable ex) {
		logger.error("Error occured", ex);
    if (ex instanceof AgileNoResultException) {
      return Response.status(202).entity(new ErrorMessage(ex.getMessage(), Response.Status.NO_CONTENT))
          .type(MediaType.APPLICATION_JSON).build();
    } else if (ex instanceof AgileDeviceNotFoundException) {
      return Response.status(404).entity(new ErrorMessage(ex.getMessage(), Response.Status.NOT_FOUND))
          .type(MediaType.APPLICATION_JSON).build();
    } else if (ex instanceof DBusExecutionException) {
      return Response.status(500).entity(new ErrorMessage(ex.getMessage(), Response.Status.INTERNAL_SERVER_ERROR))
          .type(MediaType.APPLICATION_JSON).build();
    } else if (ex instanceof WebApplicationException) {
      Response r = ((WebApplicationException) ex).getResponse();
      return Response.status(r.getStatus()).type(MediaType.APPLICATION_JSON).entity(ex.getMessage()).build();
    }
    return Response.status(500).entity(new ErrorMessage(ex)).type(MediaType.APPLICATION_JSON).build();
  }

}
