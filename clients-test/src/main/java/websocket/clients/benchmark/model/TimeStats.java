package websocket.clients.benchmark.model;

import lombok.Value;

/**
 * @author Yuriy Tumakha
 */
@Value
public class TimeStats {
  long totalTime;
  long timePerRequest;
  double min;
  double avg;
  double max;
  double max90;
  double max99;
  double max99dot9;
}
