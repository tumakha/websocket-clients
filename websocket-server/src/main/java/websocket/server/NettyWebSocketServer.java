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
import lombok.Value;
import websocket.server.netty.WebSocketServerInitializer;

import javax.net.ssl.SSLException;
import java.io.Closeable;
import java.security.cert.CertificateException;

/**
 * @author Yuriy Tumakha
 */
@Value
public class NettyWebSocketServer implements Closeable {

  boolean ssl;
  String host;
  int port;
  String webSocketPath;

  EventLoopGroup bossGroup = new NioEventLoopGroup(1);
  EventLoopGroup workerGroup = new NioEventLoopGroup();

  public Channel startChannel() throws CertificateException, SSLException, InterruptedException {
    ServerBootstrap b = new ServerBootstrap();
    b.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new WebSocketServerInitializer(createSslContext(), webSocketPath));

    System.out.println("Open your web browser and navigate to " + (ssl ? "https" : "http") + "://" + host + ":" + port);
    return b.bind(port).sync().channel();
  }

  public String getEndpoint() {
    return String.format("%s://%s:%d%s", (ssl ? "wss" : "ws"), host, port, webSocketPath);
  }

  private SslContext createSslContext() throws CertificateException, SSLException {
    if (ssl) {
      SelfSignedCertificate ssc = new SelfSignedCertificate();
      return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
    }
    return null;
  }

  @Override
  public void close() {
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
  }

}
