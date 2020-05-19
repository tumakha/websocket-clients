package websocket.clients.impl.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.SneakyThrows;
import websocket.clients.WebSocketClient;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

/**
 * @author Yuriy Tumakha
 */
public class NettyWebSocketClient implements WebSocketClient {

  private final EventLoopGroup group = new NioEventLoopGroup();
  private Channel channel;

  @Override
  public String getName() {
    return "Netty";
  }

  public void connect(String endpoint, Consumer<String> messageReader) throws URISyntaxException, InterruptedException, SSLException {
    URI uri = new URI(endpoint);
    String scheme = uri.getScheme();

    if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
      throw new IllegalArgumentException("Only WS(S) is supported.");
    }

    final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
    final int port;
    if (uri.getPort() == -1) {
      if ("ws".equalsIgnoreCase(scheme)) {
        port = 80;
      } else if ("wss".equalsIgnoreCase(scheme)) {
        port = 443;
      } else {
        port = -1;
      }
    } else {
      port = uri.getPort();
    }

    final boolean ssl = "wss".equalsIgnoreCase(scheme);
    final SslContext sslCtx;
    if (ssl) {
      sslCtx = SslContextBuilder.forClient()
          .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
    } else {
      sslCtx = null;
    }

    EventLoopGroup group = new NioEventLoopGroup();
    // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
    // If you change it to V00, ping is not supported and remember to change
    // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
    final NettyMessageHandler handler =
        new NettyMessageHandler(
            WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()),
            messageReader);

    Bootstrap b = new Bootstrap();
    b.group(group)
        .channel(NioSocketChannel.class)
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) {
            ChannelPipeline p = ch.pipeline();
            if (sslCtx != null) {
              p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
            }
            p.addLast(
                new HttpClientCodec(),
                new HttpObjectAggregator(8192),
                WebSocketClientCompressionHandler.INSTANCE,
                handler);
          }
        });

    channel = b.connect(uri.getHost(), port).sync().channel();
    handler.handshakeFuture().sync();
  }

  @Override
  public void sendMessage(String text) {
    channel.writeAndFlush(new TextWebSocketFrame(text));
  }

  @SneakyThrows
  @Override
  public void close() {
    channel.writeAndFlush(new CloseWebSocketFrame());
    group.shutdownGracefully();
  }

}
