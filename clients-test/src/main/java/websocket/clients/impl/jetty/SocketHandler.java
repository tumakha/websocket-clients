package websocket.clients.impl.jetty;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.function.Consumer;

/**
 * @author Yuriy Tumakha
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class SocketHandler {

  private final Consumer<String> messageReader;

  public SocketHandler(Consumer<String> messageReader) {
    this.messageReader = messageReader;
  }

  @OnWebSocketConnect
  public void onConnect(Session session) {
    System.out.printf("WebSocket connected: %s%n", session.getUpgradeRequest().getRequestURI());
    session.setIdleTimeout(5000);
  }

  @OnWebSocketClose
  public void onClose(int statusCode, String reason) {
    System.out.printf("WebSocket closed: %d %s%n", statusCode, reason);
  }

  @OnWebSocketMessage
  public void onMessage(String text) {
    messageReader.accept(text);
  }

  @OnWebSocketError
  public void onError(Throwable cause) {
    System.out.print("WebSocket Error: ");
    cause.printStackTrace(System.out);
  }

}
