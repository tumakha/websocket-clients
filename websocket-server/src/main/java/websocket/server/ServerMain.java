package websocket.server;

/**
 * @author Yuriy Tumakha
 */
public class ServerMain {

  public static final boolean ENABLE_SSL = true;
  public static final String HOST = "127.0.0.1";
  public static final int PORT = 8883;
  public static final String WEBSOCKET_PATH = "/websocket";


  public static void main(String[] args) throws Exception {
    if (args.length > 0 && "netty".equalsIgnoreCase(args[0])) {
      try (NettyWebSocketServer nettyServer = new NettyWebSocketServer(ENABLE_SSL, HOST, PORT, WEBSOCKET_PATH)) {
        nettyServer.startChannel().closeFuture().sync();
      }
    } else {
      new JavaWebSocketServer(ENABLE_SSL, HOST, PORT, WEBSOCKET_PATH).start();
    }
  }

}
