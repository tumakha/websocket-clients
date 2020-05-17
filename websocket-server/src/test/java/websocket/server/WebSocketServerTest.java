package websocket.server;

import io.netty.channel.Channel;
import org.junit.Test;
import websocket.client.WebSocketClient;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author Yuriy Tumakha
 */
public class WebSocketServerTest {

  private static final boolean ENABLE_SSL = true;
  private static final int TEST_PORT = 8383;
  private static final int MESSAGES_COUNT = 10;

  @Test
  public void testServer() throws Exception {
    AtomicInteger prevMessageId = new AtomicInteger();
    long[] times = new long[MESSAGES_COUNT];

    try (WebSocketServer server = new WebSocketServer(ENABLE_SSL, TEST_PORT)) {
      Channel serverChannel = server.startChannel();

      String endpoint = server.getEndpoint();
      System.out.println("WebSocket: " + endpoint);

      WebSocketClient client = new WebSocketClient(endpoint, msg -> {
        //assertThat(msg.getId(), equalTo(prevMessageId.incrementAndGet()));
        System.out.println(msg);
      });
      client.sendMessage(format("{\"request-messages\": %d}", MESSAGES_COUNT));
      client.closeChannel();

      // verify test messages

    }
  }

}
