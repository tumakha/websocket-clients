package websocket.clients.benchmark;

import lombok.SneakyThrows;
import org.springframework.util.Assert;
import websocket.clients.benchmark.model.TimeStats;
import websocket.server.json.JsonSupport;
import websocket.server.model.ResponseMsg;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.IntStream.rangeClosed;

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
    lastMessageReceived.get(180, SECONDS);
  }

  public TimeStats getStats(String clientName) {
    long totalTimeMicro = (endTestTime - startTestTime) / (long) NANO_TO_MICRO;
    long avgPerRequestMicro = totalTimeMicro / messagesCount;

    long min = Long.MAX_VALUE, max = 0;
    double avg = 0;
    int topQueueSize = messagesCount / 10;
    Queue<Long> top10percent = new PriorityQueue<>(topQueueSize);

    for (int id = 1; id <= messagesCount; id++) {
      Assert.notNull(message[id], format("Message #%d wasn't received", id));
      Assert.isTrue(receivedTime[id] != 0, format("Received time #%d wasn't saved", id));

      ResponseMsg msg = fromJson(message[id], ResponseMsg.class);
      Assert.isTrue(msg.getId() == id, format("Message #%d should have id = %d", id, msg.getId()));

      long time = receivedTime[id] - msg.getSentTime();

      min = Math.min(time, min);
      max = Math.max(time, max);
      avg = avg + (time - avg) / id;

      if (top10percent.size() < topQueueSize) {
        top10percent.add(time);
      } else if (top10percent.element() < time) {
        top10percent.remove();
        top10percent.add(time);
      }
    }

    double avgMicro = avg / NANO_TO_MICRO;
    double minMicro = min / NANO_TO_MICRO;
    double maxMicro = max / NANO_TO_MICRO;

    System.out.println("90% - " + top10percent.size());
    double max90Micro = top10percent.element() / NANO_TO_MICRO;

    rangeClosed(1, top10percent.size() * 9 / 10).forEach(i -> top10percent.remove());

    System.out.println("99% - " + top10percent.size());
    double max99Micro = top10percent.element() / NANO_TO_MICRO;

    rangeClosed(1, top10percent.size() * 9 / 10).forEach(i -> top10percent.remove());

    System.out.println("99.9% - " + top10percent.size());
    double max99dot9Micro = top10percent.element() / NANO_TO_MICRO;

    System.out.println(format("%s %d messages in %d us = Average time per message: %d us. " +
            "Message time (us): min = %.3f, avg = %.3f, max = %.3f, 90%% = %.3f, 99%% = %.3f, 99.9%% = %.3f",
        clientName, messagesCount, totalTimeMicro, avgPerRequestMicro, minMicro, avgMicro, maxMicro,
        max90Micro, max99Micro, max99dot9Micro));
    return new TimeStats(totalTimeMicro, avgPerRequestMicro,
        minMicro, avgMicro, maxMicro,
        max90Micro, max99Micro, max99dot9Micro);
  }

}
