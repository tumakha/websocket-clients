package websocket.clients;

import java.io.Closeable;
import java.util.function.Consumer;

/**
 * @author Yuriy Tumakha
 */
public interface WebSocketClient extends Closeable {

  String getName();

  void connect(String endpoint, Consumer<String> messageReader) throws Exception;

  void sendMessage(String text);

  void waitSocketClosed() throws InterruptedException;

  default void close() {
  }

}
