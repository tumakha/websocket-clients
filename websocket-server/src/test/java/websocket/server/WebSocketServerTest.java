package websocket.server;

import org.junit.Test;

/**
 * @author Yuriy Tumakha
 */
public class WebSocketServerTest {

  private static final boolean ENABLE_SSL = true;
  private static final int TEST_PORT = 8383;

  @Test
  public void testServer() throws Exception {
    try (WebSocketServer server = new WebSocketServer(ENABLE_SSL, TEST_PORT)) {
      server.startChannel();

      System.out.println(server.getPort());
    }
  }

}
