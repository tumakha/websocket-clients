package websocket.clients.benchmark.processor;


import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Function;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * @author Yuriy Tumakha
 */
public class TransformProcessor<T, R> extends SubmissionPublisher<R> implements Flow.Processor<T, R> {

  private final Function<T, R> function;
  private Flow.Subscription subscription;

  public TransformProcessor(Function<T, R> function) {
    super(newFixedThreadPool(2), 1_000_000);
    this.function = function;
  }

  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    this.subscription = subscription;
    subscription.request(1);
  }

  @Override
  public void onNext(T item) {
    submit(function.apply(item));
    subscription.request(1);
  }

  @Override
  public void onError(Throwable t) {
    t.printStackTrace();
  }

  @Override
  public void onComplete() {
    close();
  }

}
