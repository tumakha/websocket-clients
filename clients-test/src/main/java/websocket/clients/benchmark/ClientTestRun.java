package websocket.clients.benchmark;

import lombok.Value;
import org.springframework.util.Assert;
import websocket.clients.WebSocketClient;
import websocket.clients.benchmark.model.TimeStats;
import websocket.server.json.JsonSupport;
import websocket.server.model.RequestMsg;
import websocket.server.model.ResponseMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

/**
 * @author Yuriy Tumakha
 */
@Value
public class ClientTestRun implements JsonSupport {

  private static final double NANO_TO_MICRO = 1e3; // ns to us

  WebSocketClient wsClient;
  String serverEndpoint;
  int messagesCount;
  String[] message;
  long[] receivedTime;
  AtomicInteger messageId = new AtomicInteger();

  public ClientTestRun(WebSocketClient wsClient, String serverEndpoint, int messagesCount) {
    this.wsClient = wsClient;
    this.serverEndpoint = serverEndpoint;
    this.messagesCount = messagesCount;
    message = new String[messagesCount + 1];
    receivedTime = new long[messagesCount + 1];
  }

  public TimeStats run() throws Exception {
    long startTestTime = 0;

    try (wsClient) {
      wsClient.connect(serverEndpoint, this::readMessage);
      String request = toJson(new RequestMsg(messagesCount));

      startTestTime = System.nanoTime();

      wsClient.sendMessage(request);
      wsClient.waitSocketClosed();
    }

    long endTestTime = System.nanoTime();
    long totalTime = (endTestTime - startTestTime) / (long) NANO_TO_MICRO;

    return processMessages(totalTime);
  }

  private void readMessage(String text) {
    int msgId = messageId.incrementAndGet();
    receivedTime[msgId] = System.nanoTime();
    message[msgId] = text;
  }

  private TimeStats processMessages(long totalTime) {
    long avgPerRequest = totalTime / messagesCount;

    List<Long> clientTime = new ArrayList<>(messagesCount);
    for (int id = 1; id <= messagesCount; id++) {
      Assert.notNull(message[id], format("Message #%d wasn't received", id));
      Assert.isTrue(receivedTime[id] != 0, format("Received time #%d wasn't saved", id));

      ResponseMsg msg = fromJson(message[id], ResponseMsg.class);
      Assert.isTrue(msg.getId() == id, format("Message #%d should have id = %d", id, msg.getId()));

      clientTime.add(receivedTime[id] - msg.getTime());
    }

    double avg = clientTime.stream().mapToLong(l -> l).average().orElse(0) / NANO_TO_MICRO;
    double min = clientTime.stream().mapToLong(l -> l).min().orElse(0) / NANO_TO_MICRO;
    double max = clientTime.stream().mapToLong(l -> l).max().orElse(0) / NANO_TO_MICRO;

    System.out.println(format("%s %d messages in %d us = Average time per request: %d us. " +
            "Request time (us): min = %.3f, avg = %.3f, max = %.3f",
        wsClient.getName(), messagesCount, totalTime, avgPerRequest, min, avg, max));
    return new TimeStats(totalTime, avgPerRequest, min, avg, max);

  }

}
