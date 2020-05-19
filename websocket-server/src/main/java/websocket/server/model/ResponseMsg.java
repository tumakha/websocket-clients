package websocket.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Yuriy Tumakha
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMsg {
  int id;
  long sentTime;
}
