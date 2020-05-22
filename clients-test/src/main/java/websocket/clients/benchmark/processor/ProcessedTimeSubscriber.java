package websocket.clients.benchmark.processor;

import lombok.SneakyThrows;
import websocket.clients.benchmark.model.TimeStats;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.IntStream.rangeClosed;

/**
 * @author Yuriy Tumakha
 */
public class ProcessedTimeSubscriber implements Flow.Subscriber<Long> {

  private static final int SINGLE_TEST_TIMEOUT_SECONDS = 240;
  private static final double NANO_TO_MICRO = 1e3; // ns to us

  private Flow.Subscription subscription;
  private int messagesCount;
  private int count;
  private long min = Long.MAX_VALUE, max = 0;
  private double avg = 0;
  private int topQueueSize;
  Queue<Long> top10percent;

  private final CompletableFuture<Void> lastMessageReceived = new CompletableFuture<>();

  public ProcessedTimeSubscriber(int messagesCount) {
    this.messagesCount = messagesCount;
    topQueueSize = messagesCount / 10;
    top10percent = new PriorityQueue<>(topQueueSize);
  }

  @Override
  public void onSubscribe(Subscription subscription) {
    this.subscription = subscription;
    subscription.request(1);
  }

  @Override
  public void onNext(Long time) {
    min = Math.min(time, min);
    max = Math.max(time, max);
    avg = avg + (time - avg) / ++count;

    if (top10percent.size() < topQueueSize) {
      top10percent.add(time);
    } else if (top10percent.element() < time) {
      top10percent.remove();
      top10percent.add(time);
    }

    subscription.request(1);
  }

  @Override
  public void onError(Throwable t) {
    t.printStackTrace();
  }

  @Override
  public void onComplete() {
    System.out.println("Processed time collected");
    lastMessageReceived.complete(null);
  }

  @SneakyThrows
  public void waitLastMessageReceived() {
    lastMessageReceived.get(SINGLE_TEST_TIMEOUT_SECONDS, SECONDS);
  }

  public TimeStats getTimeStats() {
    double avgMicro = avg / NANO_TO_MICRO;
    double minMicro = min / NANO_TO_MICRO;
    double maxMicro = max / NANO_TO_MICRO;

    double max90Micro = top10percent.element() / NANO_TO_MICRO;

    rangeClosed(1, top10percent.size() * 9 / 10).forEach(i -> top10percent.remove()); // remove 90% of queue

    double max99Micro = top10percent.element() / NANO_TO_MICRO;

    rangeClosed(1, top10percent.size() * 9 / 10).forEach(i -> top10percent.remove()); // remove 90% of queue

    double max99dot9Micro = top10percent.element() / NANO_TO_MICRO;

    return new TimeStats(0, 0,
        minMicro, avgMicro, maxMicro,
        max90Micro, max99Micro, max99dot9Micro);
  }

}
