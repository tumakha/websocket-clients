package websocket.clients.benchmark;

import lombok.SneakyThrows;
import org.springframework.util.Assert;
import websocket.clients.benchmark.model.TimeStats;
import websocket.server.json.JsonSupport;
import websocket.server.model.ResponseMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Yuriy Tumakha
 */
public class MessageProcessor implements JsonSupport {

  private static final double NANO_TO_MICRO = 1e3; // ns to us

  private final int messagesCount;
  private final String[] message;
  private final long[] receivedTime;
  private int messageId = 0;
  long startTestTime;
  long endTestTime;
  private final CompletableFuture<Void> lastMessageReceived = new CompletableFuture<>();


  public MessageProcessor(int messagesCount) {
    this.messagesCount = messagesCount;
    message = new String[messagesCount + 1];
    receivedTime = new long[messagesCount + 1];
  }

  public void startTest() {
    startTestTime = System.nanoTime();
  }

  public void readMessage(String text) {
    int msgId = ++messageId;
    message[msgId] = text;
    receivedTime[msgId] = System.nanoTime();
    if (msgId == messagesCount) {
      endTestTime = System.nanoTime();
      lastMessageReceived.complete(null);
    }
  }

  @SneakyThrows
  public void waitLastMessageReceived() {
    lastMessageReceived.get(30, SECONDS);
  }

  public TimeStats getStats(String clientName) {
    long totalTimeMicro = (endTestTime - startTestTime) / (long) NANO_TO_MICRO;

    long avgPerRequestMicro = totalTimeMicro / messagesCount;

    List<Long> clientTime = new ArrayList<>(messagesCount);
    for (int id = 1; id <= messagesCount; id++) {
      Assert.notNull(message[id], format("Message #%d wasn't received", id));
      Assert.isTrue(receivedTime[id] != 0, format("Received time #%d wasn't saved", id));

      ResponseMsg msg = fromJson(message[id], ResponseMsg.class);
      Assert.isTrue(msg.getId() == id, format("Message #%d should have id = %d", id, msg.getId()));

      clientTime.add(receivedTime[id] - msg.getSentTime());
    }

    double avg = clientTime.stream().mapToLong(l -> l).average().orElse(0) / NANO_TO_MICRO;
    double min = clientTime.stream().mapToLong(l -> l).min().orElse(0) / NANO_TO_MICRO;
    double max = clientTime.stream().mapToLong(l -> l).max().orElse(0) / NANO_TO_MICRO;

    System.out.println(format("%s %d messages in %d us = Average time per request: %d us. " +
            "Request time (us): min = %.3f, avg = %.3f, max = %.3f",
        clientName, messagesCount, totalTimeMicro, avgPerRequestMicro, min, avg, max));
    return new TimeStats(totalTimeMicro, avgPerRequestMicro, min, avg, max);
  }

}
