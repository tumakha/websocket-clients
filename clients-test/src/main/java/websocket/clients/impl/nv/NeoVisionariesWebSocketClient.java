package websocket.clients.impl.nv;

import com.neovisionaries.ws.client.*;
import lombok.extern.slf4j.Slf4j;
import websocket.clients.WebSocketClient;
import websocket.clients.impl.java11.InsecureSslContext;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.net.http.WebSocket.NORMAL_CLOSURE;

/**
 * @author Yuriy Tumakha
 */
@Slf4j
public class NeoVisionariesWebSocketClient implements WebSocketClient {

  private WebSocket ws;

  @Override
  public String getName() {
    return "com.neovisionaries.ws.client.WebSocket";
  }

  @Override
  public void connect(String endpoint, Consumer<String> messageReader) throws Exception {
    WebSocketFactory factory = new WebSocketFactory()
        .setConnectionTimeout((int) CONNECTION_TIMEOUT.toMillis())
        .setSSLContext(InsecureSslContext.SSL_CONTEXT)
        .setVerifyHostname(false);

    ws = factory.createSocket(endpoint);

    ws.addListener(new WebSocketAdapter() {
      @Override
      public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
        log.info("WebSocket connected");
      }

      @Override
      public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame,
                                 WebSocketFrame clientCloseFrame, boolean closedByServer) {
        WebSocketFrame closeFrame = closedByServer ? serverCloseFrame : clientCloseFrame;
        log.info("Connection closed by {}: {} {}", (closedByServer? "server" : "client"),
            closeFrame.getCloseCode(), closeFrame.getCloseReason());
      }

      @Override
      public void onTextMessage(WebSocket websocket, String message) {
        messageReader.accept(message);
      }

      @Override
      public void onError(WebSocket websocket, WebSocketException cause) {
        log.error("Socket Error", cause);
      }
    });

    ws.connect();
  }

  @Override
  public void sendMessage(String text) {
    ws.sendText(text);
  }

  @Override
  public void close() {
    ws.sendClose(NORMAL_CLOSURE, "OK");
  }

}
