package websocket.clients;

import java.io.Closeable;
import java.time.Duration;
import java.util.function.Consumer;

import static java.time.Duration.ofSeconds;

/**
 * @author Yuriy Tumakha
 */
public interface WebSocketClient extends Closeable {

  Duration CONNECTION_TIMEOUT = ofSeconds(10);

  String getName();

  void connect(String endpoint, Consumer<String> messageReader) throws Exception;

  void sendMessage(String text);

  default void close() {
  }

}
