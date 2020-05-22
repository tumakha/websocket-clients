package websocket.clients;

import org.junit.jupiter.api.Test;
import websocket.server.NettyWebSocketServer;
import static websocket.server.ServerMain.*;

/**
 * @author Yuriy Tumakha
 */
public class WebSocketClientsAppTest {

  private static final int TEST_PORT = 8883;

  @Test
  public void testRunClientsTest() throws Exception {
    try (NettyWebSocketServer server = new NettyWebSocketServer(ENABLE_SSL, HOST, TEST_PORT, WEBSOCKET_PATH)) {
      server.startChannel();

      String endpoint = server.getEndpoint();
      System.out.println("Server WebSocket: " + endpoint);

      WebSocketClientsApp.main(new String[0]);
    }
  }

}
