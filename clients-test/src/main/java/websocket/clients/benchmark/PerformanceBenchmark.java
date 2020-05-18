package websocket.clients.benchmark;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import websocket.clients.WebSocketClient;
import websocket.clients.impl.java11.Java11WebSocketClient;
import websocket.clients.impl.netty.NettyWebSocketClient;
import websocket.clients.impl.spring.SpringWebSocketClient;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Yuriy Tumakha
 */
@Component
public class PerformanceBenchmark implements CommandLineRunner {

  private static final String REPORT_FILENAME = "websocket-clients-performance.csv";
  private static final String HEADER = "WebSocket Client,Requests,Total duration,Time per request,Min,Avg,Max";
  private static final AtomicInteger I = new AtomicInteger();

  @Override
  public void run(String... args) throws IOException {
    try (CsvWriter csvWriter = new CsvWriter(REPORT_FILENAME, HEADER)) {
      testClient(new Java11WebSocketClient(), csvWriter);
      testClient(new NettyWebSocketClient(), csvWriter);
      testClient(new SpringWebSocketClient(), csvWriter);
    }
    // System.exit(0);
  }

  private void testClient(WebSocketClient wsClient, CsvWriter csvWriter) {
    try (wsClient) {
      System.out.println(I.incrementAndGet() + ". " + wsClient.getName());

      csvWriter.println(wsClient.getName());
    }
  }

}
