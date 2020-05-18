package websocket.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.SneakyThrows;
import websocket.server.json.JsonSupport;
import websocket.server.model.RequestMsg;
import websocket.server.model.ResponseMsg;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

/**
 * @author Yuriy Tumakha
 */
public interface MessageHandler extends JsonSupport {

  @SneakyThrows
  default void readMessage(ChannelHandlerContext ctx, TextWebSocketFrame textFrame) {
    String request = textFrame.text();
    System.out.println("New Test Request: " + request);
    RequestMsg requestMsg = fromJson(request, RequestMsg.class);

    Channel channel = ctx.channel();
    int count = requestMsg.getRequestMessages();
    for (int id = 1; id <= count; id++) {
      String response = toJson(new ResponseMsg(id, System.nanoTime()));
      channel.writeAndFlush(new TextWebSocketFrame(response));
      MICROSECONDS.sleep(100); // run every 100 us
    }
  }

}
