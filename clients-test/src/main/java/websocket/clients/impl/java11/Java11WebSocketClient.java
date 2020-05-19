package websocket.clients.impl.java11;

import websocket.clients.WebSocketClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static java.time.Duration.ofSeconds;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Yuriy Tumakha
 */
public class Java11WebSocketClient implements WebSocketClient {

  private static final Duration TIMEOUT = ofSeconds(10);

  private final HttpClient httpClient = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .sslContext(InsecureSslContext.SSL_CONTEXT)
      .followRedirects(HttpClient.Redirect.NORMAL)
      .connectTimeout(TIMEOUT)
      .build();

  private WebSocket webSocket;

  @Override
  public String getName() {
    return "Java 11 java.net.http.WebSocket";
  }

  @Override
  public void connect(String endpoint, Consumer<String> messageReader) throws Exception {
    CompletableFuture<WebSocket> wsCompletableFuture = httpClient.newWebSocketBuilder()
        .connectTimeout(TIMEOUT)
        .buildAsync(URI.create(endpoint), new SocketListener(messageReader));

    webSocket = wsCompletableFuture.get(TIMEOUT.getSeconds(), SECONDS); // wait client connected
  }

  @Override
  public void sendMessage(String text) {
    webSocket.sendText(text, true);
  }

  @Override
  public void close() {
    webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "ok");
  }

}
