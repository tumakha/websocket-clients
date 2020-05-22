package websocket.clients.benchmark.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Yuriy Tumakha
 */
@Data
@AllArgsConstructor
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
