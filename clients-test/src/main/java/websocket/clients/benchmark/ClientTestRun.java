package websocket.clients.benchmark;

import lombok.Value;
import websocket.clients.WebSocketClient;
import websocket.clients.benchmark.model.TimeStats;
import websocket.server.json.JsonSupport;
import websocket.server.model.RequestMsg;
import websocket.server.model.ResponseMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Yuriy Tumakha
 */
@Value
public class ClientTestRun implements JsonSupport {

  private static final double NANO_TO_MICRO = 1e3; // ns to us

  WebSocketClient wsClient;
  String serverEndpoint;
  int messagesCount;
//  String[] message = new String[messagesCount];
//  long[] receivedTime = new long[messagesCount];
  List<Long> clientTime;
  AtomicInteger prevMessageId = new AtomicInteger();

  public ClientTestRun(WebSocketClient wsClient, String serverEndpoint, int messagesCount) {
    this.wsClient = wsClient;
    this.serverEndpoint = serverEndpoint;
    this.messagesCount = messagesCount;
    clientTime = new ArrayList<>(messagesCount);
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
    long avgPerRequest = totalTime / messagesCount;

    double avg = clientTime.stream().mapToLong(l -> l).average().orElse(0) / NANO_TO_MICRO;
    double min = clientTime.stream().mapToLong(l -> l).min().orElse(0) / NANO_TO_MICRO;
    double max = clientTime.stream().mapToLong(l -> l).max().orElse(0) / NANO_TO_MICRO;

    System.out.println(String.format("%s %d messages in %d us = Average time per request: %d us. " +
            "Request time (us): min = %.3f, avg = %.3f, max = %.3f",
        wsClient.getName(), messagesCount, totalTime, avgPerRequest, min, avg, max));
    return new TimeStats(totalTime, avgPerRequest, min, avg, max);
  }

  private void readMessage(String text) {
    long receivedTime = System.nanoTime();
    ResponseMsg responseMsg = fromJson(text, ResponseMsg.class);
    assert responseMsg.getId() + 1 == prevMessageId.incrementAndGet();

    clientTime.add(receivedTime - responseMsg.getTime());
  }

}
