package websocket.clients.benchmark.processor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import websocket.clients.benchmark.model.TimeStats;
import websocket.server.json.JsonSupport;
import websocket.server.model.ResponseMsg;

import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * @author Yuriy Tumakha
 */
@Slf4j
public class MessageProcessor implements JsonSupport {

  private static final double NANO_TO_MICRO = 1e3; // ns to us

  private final int messagesCount;
  private int messageId = 0;
  private long startTestTime;
  private long endTestTime;

  private final SubmissionPublisher<String> msgPublisher =
      new SubmissionPublisher<>(newFixedThreadPool(2), 1_000_000);

  private final AtomicInteger prevParsedMessageId = new AtomicInteger();

  private final TransformProcessor<String, Long> msgProcessor =
      new TransformProcessor<>(str -> {
        ResponseMsg msg = fromJson(str, ResponseMsg.class);
        Assert.isTrue(msg.getId() == prevParsedMessageId.incrementAndGet(),
            format("Unexpected Message.id = %d after %d", msg.getId(), prevParsedMessageId.intValue() - 1));
        return System.nanoTime() - msg.getSentTime();
      });

  private final ProcessedTimeSubscriber processedTimeSubscriber;


  public MessageProcessor(int messagesCount) {
    this.messagesCount = messagesCount;
    processedTimeSubscriber = new ProcessedTimeSubscriber(messagesCount);
    msgPublisher.subscribe(msgProcessor);
    msgProcessor.subscribe(processedTimeSubscriber);
  }

  public void startTest() {
    startTestTime = System.nanoTime();
  }

  public void readMessage(String text) {
    msgPublisher.submit(text);

    if (++messageId == messagesCount) {
      endTestTime = System.nanoTime();
      log.info("Last message received");
      msgPublisher.close();
    }
  }

  @SneakyThrows
  public void waitLastMessageReceived() {
    processedTimeSubscriber.waitLastMessageReceived();
  }

  public TimeStats getStats(String clientName) {
    long totalTimeMicro = (endTestTime - startTestTime) / (long) NANO_TO_MICRO;
    long avgPerRequestMicro = totalTimeMicro / messagesCount;

    TimeStats stats = processedTimeSubscriber.getTimeStats();
    stats.setTotalTime(totalTimeMicro);
    stats.setTimePerRequest(avgPerRequestMicro);

    System.out.println(format("%s %d messages in %d us = Average time per message: %d us. " +
            "Message time (us): min = %.3f, avg = %.3f, max = %.3f, 90%% = %.3f, 99%% = %.3f, 99.9%% = %.3f",
        clientName, messagesCount, totalTimeMicro, avgPerRequestMicro, stats.getMin(), stats.getAvg(), stats.getMax(),
        stats.getMax90(), stats.getMax99(), stats.getMax99dot9()));
    return stats;
  }

}
