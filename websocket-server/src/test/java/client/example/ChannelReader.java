package client.example;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

public class ChannelReader extends SimpleChannelInboundHandler<Object> {

  private final WebSocketClientHandshaker handshaker;
  private ChannelPromise handshakeFuture;

  public ChannelReader(WebSocketClientHandshaker handshaker) {
    this.handshaker = handshaker;
  }

  public ChannelFuture handshakeFuture() {
    return handshakeFuture;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {
    handshakeFuture = ctx.newPromise();
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    handshaker.handshake(ctx.channel());
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    System.out.println("WebSocket Client disconnected!");
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    Channel ch = ctx.channel();
    if (!handshaker.isHandshakeComplete()) {
      try {
        handshaker.finishHandshake(ch, (FullHttpResponse) msg);
        System.out.println("WebSocket Client connected!");
        handshakeFuture.setSuccess();
      } catch (WebSocketHandshakeException e) {
        System.out.println("WebSocket Client failed to connect");
        handshakeFuture.setFailure(e);
      }
      return;
    }

    if (msg instanceof FullHttpResponse) {
      FullHttpResponse response = (FullHttpResponse) msg;
      throw new IllegalStateException(
          "Unexpected FullHttpResponse (getStatus=" + response.status() +
              ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
    }

    WebSocketFrame frame = (WebSocketFrame) msg;
    if (frame instanceof TextWebSocketFrame) {
      TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
      System.out.println("WebSocket Client received message: " + textFrame.text());
    } else if (frame instanceof PongWebSocketFrame) {
      System.out.println("WebSocket Client received pong");
    } else if (frame instanceof CloseWebSocketFrame) {
      System.out.println("WebSocket Client received closing");
      ch.close();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    if (!handshakeFuture.isDone()) {
      handshakeFuture.setFailure(cause);
    }
    ctx.close();
  }

}