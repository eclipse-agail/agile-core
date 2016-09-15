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
package iot.agile.http.ws;

import java.io.IOException;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.Session;

/**
 *
 * @author Csaba Kiraly <kiraly@fbk.eu>
 */
@WebSocket
public class AgileWebSocketAdapter extends WebSocketAdapter {

  private Session session;

  @Override
  public void onWebSocketConnect(Session sess) {
    session = sess;
    System.out.printf("New websocket connection %s%n", sess.getRemoteAddress());
  }

  @Override
  public void onWebSocketText(String message) {
    if (isConnected()) {
      try {
        System.out.printf("Echoing back message [%s]%n", message);
        getRemote().sendString(message);
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
    }
  }

  

}
