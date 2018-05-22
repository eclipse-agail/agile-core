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

package org.eclipse.agail.http;

import org.eclipse.agail.http.exception.AgileExceptionMapper;
import org.eclipse.agail.http.filter.CORSResponseFilter;
import org.eclipse.agail.http.service.DbusClient;
import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@ApplicationPath("/")
public class AgileApplication extends ResourceConfig {

  protected class AgileBinder extends AbstractBinder {

    @Override
    protected void configure() {
      bind(DbusClient.class).to(DbusClient.class)
              .in(Singleton.class);
    }
  }

  public AgileApplication() {

    register(JacksonFeature.class);
    
    register(new AgileBinder());
    register(new AgileExceptionMapper());
    register(CORSResponseFilter.class);

    packages("org.eclipse.agail.http.resource;");
    

    
  }
}
