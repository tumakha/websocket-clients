package websocket.clients.benchmark;

import lombok.Value;
import websocket.clients.WebSocketClient;
import websocket.clients.benchmark.model.TimeStats;
import websocket.clients.benchmark.processor.MessageProcessor;
import websocket.server.json.JsonSupport;
import websocket.server.model.RequestMsg;

/**
 * @author Yuriy Tumakha
 */
@Value
public class ClientTestRun implements JsonSupport {

  WebSocketClient wsClient;
  String serverEndpoint;
  int messagesCount;

  public TimeStats run() throws Exception {
    MessageProcessor messageProcessor = new MessageProcessor(messagesCount);
    try (wsClient) {
      wsClient.connect(serverEndpoint, messageProcessor::readMessage);
      String request = toJson(new RequestMsg(messagesCount));

      messageProcessor.startTest();
      wsClient.sendMessage(request);
      messageProcessor.waitLastMessageReceived();
    }
    return messageProcessor.getStats(wsClient.getName());
  }

}
