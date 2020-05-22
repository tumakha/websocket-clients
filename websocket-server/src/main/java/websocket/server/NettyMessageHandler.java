package websocket.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannelConfig;
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
public interface NettyMessageHandler extends JsonSupport {

  @SneakyThrows
  default void readMessage(ChannelHandlerContext ctx, String message) {
    System.out.println("New Test Request: " + message);
    RequestMsg requestMsg = fromJson(message, RequestMsg.class);

    final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
    final AtomicInteger messageId = new AtomicInteger();
    final int count = requestMsg.getRequestMessages();
    final Channel channel = ctx.channel();
    final SocketChannelConfig config = (SocketChannelConfig) channel.config();
    System.out.println("TCP_NODELAY: " + config.isTcpNoDelay());

    scheduledExecutor.scheduleAtFixedRate(() -> {
      int msgId = messageId.incrementAndGet();
      if (msgId <= count) {
        String response = toJson(new ResponseMsg(msgId, System.nanoTime()));
        channel.writeAndFlush(new TextWebSocketFrame(response));
      } else {
        scheduledExecutor.shutdown();
      }
    }, 0, 200, MICROSECONDS); // run every 200 us
  }

}
