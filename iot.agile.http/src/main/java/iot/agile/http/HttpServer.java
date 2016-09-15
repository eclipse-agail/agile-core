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
package iot.agile.http;

import ch.qos.logback.classic.ViewStatusMessagesServlet;
import iot.agile.http.ws.MyEchoSocket;
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
            factory.register(MyEchoSocket.class);
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
