package websocket.clients.impl.jetty;

import lombok.SneakyThrows;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static java.time.Duration.ofSeconds;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Yuriy Tumakha
 */
public class JettyWebSocketClient implements websocket.clients.WebSocketClient {

  private final HttpClient http;
  private final WebSocketClient client;
  private Session session;

  public JettyWebSocketClient(){
    SslContextFactory ssl = new SslContextFactory.Client();
    ssl.setTrustAll(true); // accept self-signed certificates on localhost
    ssl.addExcludeProtocols("tls/1.3");
    //ssl.setExcludeCipherSuites();
    http = new HttpClient(ssl);
    client = new WebSocketClient(http);
  }

  @Override
  public String getName() {
    return "Jetty";
  }

  @Override
  public void connect(String endpoint, Consumer<String> messageReader) throws Exception {
    http.start();
    client.start();

    URI uri = URI.create(endpoint);
    SocketHandler socket = new SocketHandler(messageReader);
    ClientUpgradeRequest request = new ClientUpgradeRequest();
    Future<Session> sessionFuture = client.connect(socket, uri, request);
    session = sessionFuture.get(CONNECTION_TIMEOUT.getSeconds(), SECONDS); // wait client connected
  }

  @Override
  public void sendMessage(String text) {
    session.getRemote().sendStringByFuture(text);
  }

  @SneakyThrows
  @Override
  public void close() {
    session.close(WebSocket.NORMAL_CLOSURE, "OK");
    http.stop();
    client.stop();
  }

}
