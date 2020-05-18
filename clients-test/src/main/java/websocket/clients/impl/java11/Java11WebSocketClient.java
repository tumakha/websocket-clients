package websocket.clients.impl.java11;

import websocket.clients.WebSocketClient;
import java.net.http.WebSocket;
import java.util.function.Consumer;

/**
 * @author Yuriy Tumakha
 */
public class Java11WebSocketClient implements WebSocketClient {

  @Override
  public String getName() {
    return "Java 11 java.net.http.WebSocket";
  }

  @Override
  public void connect(String endpoint, Consumer<String> messageReader) throws Exception {

  }

  @Override
  public void sendMessage(String text) {

  }

  @Override
  public void waitSocketClosed() throws InterruptedException {

  }

}
