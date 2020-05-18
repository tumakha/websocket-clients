package websocket.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Yuriy Tumakha
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestMsg {

  @JsonProperty("request-messages")
  int requestMessages;
}
