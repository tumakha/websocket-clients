package websocket.server;

import lombok.Value;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import websocket.server.json.JsonSupport;
import websocket.server.model.RequestMsg;
import websocket.server.model.ResponseMsg;
import websocket.server.ssl.SelfSignedSslContext;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

/**
 * @author Yuriy Tumakha
 */
@Value
@Slf4j
public class JavaWebSocketServer implements JsonSupport {

    boolean ssl;
    String host;
    int port;
    String webSocketPath;

    public void start() {
        WebSocketServer server = new WebSocketServer(new InetSocketAddress(port)) {

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                log.info("New connection: {}", conn.getRemoteSocketAddress());
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                log.info("Closed connection {}", conn.getRemoteSocketAddress());
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                System.out.println("New Test Request: " + message);
                RequestMsg requestMsg = fromJson(message, RequestMsg.class);

                final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
                final AtomicInteger messageId = new AtomicInteger();
                final int count = requestMsg.getRequestMessages();

                scheduledExecutor.scheduleAtFixedRate(() -> {
                    int msgId = messageId.incrementAndGet();
                    if (msgId <= count) {
                        String response = toJson(new ResponseMsg(msgId, System.nanoTime()));
                        conn.send(response);
                    } else {
                        scheduledExecutor.shutdown();
                    }
                }, 0, 200, MICROSECONDS); // run every 200 us
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                log.error(conn.getRemoteSocketAddress() + " socket error", ex);
            }

            @Override
            public void onStart() {
                System.out.println("Java-WebSocket Server started");
            }
        };

        if (ssl) {
            server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(SelfSignedSslContext.SSL_CONTEXT));
        }
        server.setTcpNoDelay(true);
        server.start();
    }

    public String getEndpoint() {
        return String.format("wss://%s:%d%s", host, port, webSocketPath);
    }

}
