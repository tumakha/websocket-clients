package websocket.clients.impl.javawebsocket;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import websocket.clients.impl.java11.InsecureSslContext;

import java.net.URI;
import java.util.function.Consumer;

/**
 * org.java_websocket.client.WebSocketClient wrapper.
 *
 * @author Yuriy Tumakha
 */
@Slf4j
public class JavaWebSocketClient implements websocket.clients.WebSocketClient {

    private WebSocketClient client;

    @Override
    public String getName() {
        return "Java-WebSocket";
    }

    @Override
    public void connect(String endpoint, Consumer<String> messageReader) throws InterruptedException {
        client = new WebSocketClient(URI.create(endpoint), new Draft_6455()) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                log.info("WebSocket connection opened");
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                log.info("Connection closed by {}: {} {}", (remote ? "server" : "client"), code, reason);
                //client.close();
            }

            @Override
            public void onMessage(String message) {
                messageReader.accept(message);
            }

            @Override
            public void onError(Exception ex) {
                log.error("Socket Error", ex);
            }
        };
        client.setSocketFactory(InsecureSslContext.SSL_CONTEXT.getSocketFactory());
        client.setTcpNoDelay(true);
        client.connectBlocking();
    }

    @Override
    public void sendMessage(String text) {
        client.send(text);
    }

    @Override
    public void close() {
        client.close();
    }

}
