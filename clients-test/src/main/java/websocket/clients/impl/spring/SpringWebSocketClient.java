package websocket.clients.impl.spring;

import websocket.clients.WebSocketClient;

/**
 * Wrapper for {@link org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient}.
 *
 * @author Yuriy Tumakha
 */
public class SpringWebSocketClient implements WebSocketClient {

  @Override
  public String getName() {
    return "Spring ReactorNettyWebSocketClient";
  }

}