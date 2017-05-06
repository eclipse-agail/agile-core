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
package iot.agile.http.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

public class CORSResponseFilter implements ContainerResponseFilter {

//  @Override
  public void filter(ContainerRequestContext request, ContainerResponseContext response) {

      response.getHeaders().add("Powered-By", "Agile");

      String origin = request.getHeaderString("Origin");

      response.getHeaders().add("Access-Control-Allow-Credentials", "true");
      response.getHeaders().add("Access-Control-Allow-Origin", origin);

      response.getHeaders().add("Access-Control-Allow-Headers", "Authorization,Content-Type");
      response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
      response.getHeaders().add("Access-Control-Max-Age", "3600");

  }
}

