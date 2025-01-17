package websocket.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import websocket.server.NettyMessageHandler;

/**
 * Read socket frames.
 *
 * @author Yuriy Tumakha
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> implements NettyMessageHandler {

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);
    System.out.println("Open channel " + ctx.channel());
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    System.out.println("Close channel " + ctx.channel());
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
    // ping and pong frames already handled
    if (frame instanceof TextWebSocketFrame) {
      readMessage(ctx, ((TextWebSocketFrame) frame).text());
    } else {
      String message = "unsupported frame type: " + frame.getClass().getName();
      throw new UnsupportedOperationException(message);
    }
  }

}
