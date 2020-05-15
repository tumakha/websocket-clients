package websocket.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.Locale;

/**
 * Echoes uppercase content of text frames.
 *
 * @author Yuriy Tumakha
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);
    System.out.println("New channel " + ctx.channel().id());
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
    // ping and pong frames already handled

    if (frame instanceof TextWebSocketFrame) {
      // Send the uppercase string back.
      String request = ((TextWebSocketFrame) frame).text();
      ctx.channel().writeAndFlush(new TextWebSocketFrame(request.toUpperCase(Locale.US)));
    } else {
      String message = "unsupported frame type: " + frame.getClass().getName();
      throw new UnsupportedOperationException(message);
    }
  }

}
