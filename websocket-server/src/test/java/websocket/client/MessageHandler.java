package websocket.client;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import websocket.server.json.JsonSupport;
import websocket.server.model.ResponseMsg;

import java.util.function.Consumer;

public class MessageHandler extends SimpleChannelInboundHandler<Object> implements JsonSupport {

  private final WebSocketClientHandshaker handShaker;
  private final Consumer<ResponseMsg> messageReader;
  private ChannelPromise handshakeFuture;

  public MessageHandler(WebSocketClientHandshaker handShaker, Consumer<ResponseMsg> messageReader) {
    this.handShaker = handShaker;
    this.messageReader = messageReader;
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
    handShaker.handshake(ctx.channel());
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    System.out.println("WebSocket Client disconnected!");
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, Object msg) {
    Channel ch = ctx.channel();
    if (!handShaker.isHandshakeComplete()) {
      try {
        handShaker.finishHandshake(ch, (FullHttpResponse) msg);
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
      String text = textFrame.text();
      System.out.println("WebSocket Client received message: " + text);
      ResponseMsg response = fromJson(text, ResponseMsg.class);
      messageReader.accept(response);
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