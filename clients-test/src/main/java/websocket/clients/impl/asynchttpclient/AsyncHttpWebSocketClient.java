package websocket.clients.impl.asynchttpclient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import websocket.clients.WebSocketClient;

import java.util.function.Consumer;

import static java.net.http.WebSocket.NORMAL_CLOSURE;
import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

/**
 * @author Yuriy Tumakha
 */
@Slf4j
public class AsyncHttpWebSocketClient implements WebSocketClient {

    private AsyncHttpClient asyncHttpClient;
    private WebSocket webSocket;

    @Override
    public String getName() {
        return "AsyncHttpClient";
    }

    @Override
    public void connect(String endpoint, Consumer<String> messageReader) throws Exception {
        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();

        AsyncHttpClientConfig clientConfig = config()
                .setConnectTimeout((int) CONNECTION_TIMEOUT.toMillis())
                .setRequestTimeout((int) CONNECTION_TIMEOUT.toMillis())
                .setSslContext(sslContext)
                .build();
        asyncHttpClient = asyncHttpClient(clientConfig);

        WebSocketUpgradeHandler.Builder upgradeHandlerBuilder = new WebSocketUpgradeHandler.Builder();
        WebSocketUpgradeHandler wsHandler = upgradeHandlerBuilder.addWebSocketListener(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket websocket) {
                log.info("WebSocket connection opened");
            }

            @SneakyThrows
            @Override
            public void onClose(WebSocket websocket, int code, String reason) {
                log.info("WebSocket connection closed: {} {}", code, reason);
                asyncHttpClient.close();
            }

            @Override
            public void onTextFrame(String payload, boolean finalFragment, int rsv) {
                messageReader.accept(payload);
            }

            @Override
            public void onError(Throwable ex) {
                log.error("Socket Error", ex);
            }
        }).build();

        webSocket = asyncHttpClient.prepareGet(endpoint).execute(wsHandler).get();
    }

    @Override
    public void sendMessage(String text) {
        webSocket.sendTextFrame(text);
    }

    @SneakyThrows
    @Override
    public void close() {
        webSocket.sendCloseFrame(NORMAL_CLOSURE, "OK");
    }

}
