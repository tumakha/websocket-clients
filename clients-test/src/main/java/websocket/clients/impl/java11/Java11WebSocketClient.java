package websocket.clients.impl.java11;

import websocket.clients.WebSocketClient;
import java.net.http.WebSocket;

/**
 * @author Yuriy Tumakha
 */
public class Java11WebSocketClient implements WebSocketClient {

  @Override
  public String getName() {
    return "Java 11 java.net.http.WebSocket";
  }

}
