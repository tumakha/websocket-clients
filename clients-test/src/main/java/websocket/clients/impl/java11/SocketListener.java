package websocket.clients.impl.java11;

import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * @author Yuriy Tumakha
 */
public class SocketListener implements Listener {

  private final Consumer<String> messageReader;

  public SocketListener(Consumer<String> messageReader) {
    this.messageReader = messageReader;
  }

  @Override
  public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
    messageReader.accept(data.toString());
    return Listener.super.onText(webSocket, data, last);
  }

  @Override
  public void onOpen(WebSocket webSocket) {
    System.out.println("WebSocket connected");
    Listener.super.onOpen(webSocket);
  }

  @Override
  public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
    System.out.println("WebSocket disconnected: " + statusCode + " " + reason);
    return Listener.super.onClose(webSocket, statusCode, reason);
  }

}
