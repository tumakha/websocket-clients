package websocket.clients.impl.spring;

import websocket.clients.WebSocketClient;

import java.util.function.Consumer;

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

  @Override
  public void connect(String endpoint, Consumer<String> messageReader) throws Exception {

  }

  @Override
  public void sendMessage(String text) {

  }

}