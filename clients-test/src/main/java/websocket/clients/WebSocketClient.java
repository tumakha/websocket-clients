package websocket.clients;

import java.io.Closeable;

/**
 * @author Yuriy Tumakha
 */
public interface WebSocketClient extends Closeable {

  String getName();

  default void close() {
  }

}
