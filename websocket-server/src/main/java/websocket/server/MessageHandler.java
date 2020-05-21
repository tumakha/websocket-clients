package websocket.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.SneakyThrows;
import websocket.server.json.JsonSupport;
import websocket.server.model.RequestMsg;
import websocket.server.model.ResponseMsg;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

/**
 * @author Yuriy Tumakha
 */
public interface MessageHandler extends JsonSupport {

  @SneakyThrows
  default void readMessage(ChannelHandlerContext ctx, String msg) {
    System.out.println("New Test Request: " + msg);
    RequestMsg requestMsg = fromJson(msg, RequestMsg.class);

    final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
    final AtomicInteger messageId = new AtomicInteger();
    final int count = requestMsg.getRequestMessages();
    final Channel channel = ctx.channel();

    scheduledExecutor.scheduleAtFixedRate(() -> {
      int msgId = messageId.incrementAndGet();
      if (msgId <= count) {
        String response = toJson(new ResponseMsg(msgId, System.nanoTime()));
        channel.writeAndFlush(new TextWebSocketFrame(response));
      } else {
        scheduledExecutor.shutdown();
      }
    }, 0, 100, MICROSECONDS); // run every 100 us
  }

}
