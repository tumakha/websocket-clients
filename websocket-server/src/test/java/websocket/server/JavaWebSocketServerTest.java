package websocket.server;

import org.junit.Test;
import websocket.client.WebSocketClient;
import websocket.server.model.RequestMsg;
import websocket.server.model.ResponseMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static websocket.server.ServerMain.*;

/**
 * @author Yuriy Tumakha
 */
public class JavaWebSocketServerTest {

    private static final int TEST_PORT = 8881;
    private static final int MESSAGES_COUNT = 10;

    @Test
    public void testServer() throws Exception {
        List<ResponseMsg> messages = new ArrayList<>(MESSAGES_COUNT);

        JavaWebSocketServer server = new JavaWebSocketServer(ENABLE_SSL, HOST, TEST_PORT, WEBSOCKET_PATH);
        server.start();

            String endpoint = server.getEndpoint();
            System.out.println("WebSocket: " + endpoint);

            try (WebSocketClient client = new WebSocketClient(endpoint, messages::add)) {
                client.sendMessage(new RequestMsg(MESSAGES_COUNT));

                while (messages.size() < MESSAGES_COUNT){
                    MILLISECONDS.sleep(300);
                }
            }

            // verify test messages
            assertThat(messages, hasSize(MESSAGES_COUNT));

            final AtomicInteger prevMessageId = new AtomicInteger();
            messages.forEach(msg -> assertThat(msg.getId(), equalTo(prevMessageId.incrementAndGet())));

            messages.stream().map(ResponseMsg::getSentTime).reduce((t1, t2) -> {
                assertThat(t2 - t1, lessThan((long) 1e9));
                return t2;
            });

        }

}
