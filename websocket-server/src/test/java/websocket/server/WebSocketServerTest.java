package websocket.server;

import org.junit.Test;
import websocket.client.WebSocketClient;
import websocket.server.model.RequestMsg;
import websocket.server.model.ResponseMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.number.OrderingComparison.lessThan;

/**
 * @author Yuriy Tumakha
 */
public class WebSocketServerTest {

  private static final boolean ENABLE_SSL = true;
  private static final int TEST_PORT = 8383;
  private static final int MESSAGES_COUNT = 10;

  @Test
  public void testServer() throws Exception {
    List<ResponseMsg> messages = new ArrayList<>(MESSAGES_COUNT);

    try (WebSocketServer server = new WebSocketServer(ENABLE_SSL, TEST_PORT)) {
      server.startChannel();

      String endpoint = server.getEndpoint();
      System.out.println("WebSocket: " + endpoint);

      try (WebSocketClient client = new WebSocketClient(endpoint, messages::add)) {
        client.sendMessage(new RequestMsg(MESSAGES_COUNT));
        client.closeChannel();

        // verify test messages
        assertThat(messages, hasSize(MESSAGES_COUNT));

        final AtomicInteger prevMessageId = new AtomicInteger();
        messages.forEach(msg -> assertThat(msg.getId(), equalTo(prevMessageId.incrementAndGet())));

        messages.stream().map(ResponseMsg::getTime).reduce((t1, t2) -> {
          assertThat(t2 - t1, lessThan((long) 1e9));
          return t2;
        });
      }
    }
  }

}
