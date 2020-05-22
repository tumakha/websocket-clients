package websocket.clients.impl.spring;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import websocket.clients.impl.java11.InsecureSslContext;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.tomcat.websocket.Constants.SSL_CONTEXT_PROPERTY;
import static org.springframework.web.socket.CloseStatus.NORMAL;

/**
 * @author Yuriy Tumakha
 */
@Slf4j
public class SpringStandardWebSocketClient implements websocket.clients.WebSocketClient {

  private WebSocketSession webSocketSession;

  @Override
  public String getName() {
    return "Spring StandardWebSocketClient";
  }

  @Override
  public void connect(String endpoint, Consumer<String> messageReader) throws Exception {
    StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
    webSocketClient.setUserProperties(Map.of(SSL_CONTEXT_PROPERTY, InsecureSslContext.SSL_CONTEXT));

    webSocketSession = webSocketClient.doHandshake(new TextWebSocketHandler() {
      @Override
      public void handleTextMessage(WebSocketSession session, TextMessage message) {
        messageReader.accept(message.getPayload());
      }

      @Override
      public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Established connection - " + session);
      }

      @Override
      public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Connection closed: {} {}", status.getCode(), status.getReason());
      }

    }, new WebSocketHttpHeaders(), URI.create(endpoint))
        .get(CONNECTION_TIMEOUT.getSeconds(), SECONDS); // wait client connected
  }

  @SneakyThrows
  @Override
  public void sendMessage(String text) {
    webSocketSession.sendMessage(new TextMessage(text));
  }

  @SneakyThrows
  @Override
  public void close() {
    webSocketSession.close(NORMAL.withReason("OK"));
  }

}
