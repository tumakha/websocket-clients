package websocket.clients.impl.netty;

import websocket.clients.WebSocketClient;

/**
 * @author Yuriy Tumakha
 */
public class NettyWebSocketClient implements WebSocketClient {

  @Override
  public String getName() {
    return "Netty";
  }

}