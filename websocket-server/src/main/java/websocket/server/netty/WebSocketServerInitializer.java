package websocket.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;

/**
 * @author Yuriy Tumakha
 */
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

  private final SslContext sslCtx;
  private final String websocketPath;

  public WebSocketServerInitializer(SslContext sslCtx, String websocketPath) {
    this.sslCtx = sslCtx;
    this.websocketPath = websocketPath;
  }

  @Override
  public void initChannel(SocketChannel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    if (sslCtx != null) {
      pipeline.addLast(sslCtx.newHandler(ch.alloc()));
    }
    pipeline.addLast(new HttpServerCodec());
    pipeline.addLast(new HttpObjectAggregator(65536));
    pipeline.addLast(new WebSocketServerCompressionHandler());
    pipeline.addLast(new WebSocketServerProtocolHandler(websocketPath, null, true));
    pipeline.addLast(new WebSocketIndexPageHandler(websocketPath));
    pipeline.addLast(new WebSocketFrameHandler());
  }

}
