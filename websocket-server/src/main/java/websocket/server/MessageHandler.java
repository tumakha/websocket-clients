package websocket.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import websocket.server.json.JsonSupport;
import websocket.server.model.RequestMsg;
import websocket.server.model.ResponseMsg;

import static java.util.stream.IntStream.rangeClosed;

/**
 * @author Yuriy Tumakha
 */
public interface MessageHandler extends JsonSupport {

  default void readMessage(ChannelHandlerContext ctx, TextWebSocketFrame textFrame) {
    String request = textFrame.text();
    System.out.println("New Test Request: " + request);
    RequestMsg requestMsg = fromJson(request, RequestMsg.class);

    rangeClosed(1, requestMsg.getRequestMessages()).forEach(i -> {
      String response = toJson(new ResponseMsg(i, System.nanoTime()));
      ctx.channel().writeAndFlush(new TextWebSocketFrame(response));
    });
  }

}
