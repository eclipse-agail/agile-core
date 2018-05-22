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

import ch.qos.logback.classic.ViewStatusMessagesServlet;
import org.eclipse.agail.http.ws.AgileWebSocketAdapter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class HttpServer {
  
  protected int port = 8080;
  
  protected Server server;

  public static void main(String[] args) throws Exception {
    (new HttpServer()).launch();
  }
  
  public void launch() throws InterruptedException, Exception {
    
    int port = this.port;
    try{
      String sport = System.getProperty("PORT");
      if(sport != null)
        port = new Integer(sport);
    }
    catch(Exception e) {
      System.out.println("Cannot parse port");
    }
      

    server = new Server(port);
    ServletContextHandler context = new ServletContextHandler(server, "/");

    
    // register WS handler
    WebSocketServlet wsServlet = new WebSocketServlet()
    {
        @Override
        public void configure(WebSocketServletFactory factory)
        {
            factory.register(AgileWebSocketAdapter.class);
        }
    };
    context.addServlet(new ServletHolder(wsServlet), "/ws/*");
    
    // register HTTP API servlet
    
    ResourceConfig res = new AgileApplication();
    ServletHolder servlet = new ServletHolder(new ServletContainer(res));
    context.addServlet(servlet, "/api/*");
    
    ServletHolder logbackServlet = new ServletHolder(new ViewStatusMessagesServlet());
    context.addServlet(logbackServlet, "/logs");
    
    
    server.start();
  }
  
  public void stop() {
    if(server != null) {
      server.destroy();
      server = null;
    }
  }
}
