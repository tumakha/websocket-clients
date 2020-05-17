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
public class WebSocketServer implements Closeable {

  private static final boolean ENABLE_SSL = true;
  private static final String HOST = "127.0.0.1";
  private static final int PORT = 8883;
  private static final String WEBSOCKET_PATH = "/websocket";

  boolean ssl;
  int port;
  EventLoopGroup bossGroup = new NioEventLoopGroup(1);
  EventLoopGroup workerGroup = new NioEventLoopGroup();


  public static void main(String[] args) throws Exception {
    try (WebSocketServer server = new WebSocketServer(ENABLE_SSL, PORT)) {
      server.startChannel().closeFuture().sync();
    }
  }

  public Channel startChannel() throws CertificateException, SSLException, InterruptedException {
    ServerBootstrap b = new ServerBootstrap();
    b.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new WebSocketServerInitializer(createSslContext(), WEBSOCKET_PATH));

    System.out.println("Open your web browser and navigate to " + (ssl ? "https" : "http") + "://" + HOST + ":" + port);
    return b.bind(port).sync().channel();
  }

  public String getEndpoint() {
    return String.format("%s://%s:%d%s", (ssl ? "wss" : "ws"), HOST, port, WEBSOCKET_PATH);
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
