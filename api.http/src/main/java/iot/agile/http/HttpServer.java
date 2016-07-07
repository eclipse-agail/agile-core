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

import iot.agile.http.ws.MyEchoSocket;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
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

    server = new Server(8080);

    ResourceConfig res = new AgileApplication();
    
    ServletHolder servlet = new ServletHolder(new ServletContainer(res));
    
    // register WS handler
    ServletContextHandler wsContext = new ServletContextHandler(server, "/*");
    WebSocketHandler wsHandler = new WebSocketHandler()
    {
        @Override
        public void configure(WebSocketServletFactory factory)
        {
            factory.register(MyEchoSocket.class);
        }
    };
    
    wsContext.setContextPath("/*");
    wsContext.setHandler(wsHandler);
    
    // register HTTP API servlet
    ServletContextHandler context = new ServletContextHandler(server, "/*");
    context.addServlet(servlet, "/*");

    server.start();
  }
  
  public void stop() {
    if(server != null) {
      server.destroy();
      server = null;
    }
  }
}
