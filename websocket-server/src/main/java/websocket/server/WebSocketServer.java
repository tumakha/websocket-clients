package websocket.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import websocket.server.netty.WebSocketServerInitializer;

/**
 * @author Yuriy Tumakha
 */
public final class WebSocketServer {

  private static final boolean ENABLE_SSL = true;
  private static final int HTTP_PORT = 8880;
  private static final int HTTPS_PORT = 8883;
  private static final int PORT = ENABLE_SSL ? HTTPS_PORT : HTTP_PORT;

  public static void main(String[] args) throws Exception {
    // Configure SSL.
    SslContext sslCtx = null;
    if (ENABLE_SSL) {
      SelfSignedCertificate ssc = new SelfSignedCertificate();
      sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
    }

    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(new WebSocketServerInitializer(sslCtx));

      Channel ch = b.bind(PORT).sync().channel();
      System.out.println("Open your web browser and navigate to " +
          (ENABLE_SSL ? "https" : "http") + "://127.0.0.1:" + PORT + '/');

      ch.closeFuture().sync();
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

}
