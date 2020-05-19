package websocket.clients.impl.spring;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import websocket.clients.WebSocketClient;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.time.Duration;
import java.util.function.Consumer;

import static java.time.Duration.ofSeconds;

/**
 * Wrapper for {@link org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient}.
 *
 * @author Yuriy Tumakha
 */
@Slf4j
public class SpringWebSocketClient implements WebSocketClient {

  private static final Duration TIMEOUT = ofSeconds(10);
  private String endpoint;
  private Consumer<String> messageReader;

  private final SslContext sslContext = SslContextBuilder.forClient()
      .trustManager(InsecureTrustManagerFactory.INSTANCE).build();

  private final HttpClient httpClient = HttpClient.create().secure(ssl -> ssl.sslContext(sslContext));

  private final org.springframework.web.reactive.socket.client.WebSocketClient client =
      new ReactorNettyWebSocketClient(httpClient);

  private Disposable executeDisposable;

  public SpringWebSocketClient() throws SSLException {
  }

  @Override
  public String getName() {
    return "Spring ReactorNettyWebSocketClient";
  }

  @Override
  public void connect(String endpoint, Consumer<String> messageReader) {
    this.endpoint = endpoint;
    this.messageReader = messageReader;
  }

  @Override
  public void sendMessage(String text) {
    log.info("Send message: {}", text);

    executeDisposable = client.execute(URI.create(endpoint), session -> {
      Mono<Void> sendMono = session.send(
          Mono.just(session.textMessage(text))
      );

      Flux<String> receiveFlux = session.receive()
          .map(WebSocketMessage::getPayloadAsText)
          .doOnNext(messageReader);

      return sendMono
          .thenMany(receiveFlux)
          .then();
    }).subscribe();
  }

  @Override
  public void close() {
    log.info("Client closed");
    executeDisposable.dispose();
  }

}
