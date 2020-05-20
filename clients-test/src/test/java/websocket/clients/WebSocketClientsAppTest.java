package websocket.clients;

import org.junit.jupiter.api.Test;
import websocket.server.WebSocketServer;

/**
 * @author Yuriy Tumakha
 */
public class WebSocketClientsAppTest {

  private static final boolean ENABLE_SSL = true;
  private static final int TEST_PORT = 8883;

  @Test
  public void testRunClientsTest() throws Exception {
    try (WebSocketServer server = new WebSocketServer(ENABLE_SSL, TEST_PORT)) {
      server.startChannel();

      String endpoint = server.getEndpoint();
      System.out.println("Server WebSocket: " + endpoint);

      WebSocketClientsApp.main(new String[0]);
    }
  }

}
