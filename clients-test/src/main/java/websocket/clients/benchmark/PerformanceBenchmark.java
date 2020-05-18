package websocket.clients.benchmark;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import websocket.clients.WebSocketClient;
import websocket.clients.benchmark.model.TimeStats;
import websocket.clients.impl.java11.Java11WebSocketClient;
import websocket.clients.impl.netty.NettyWebSocketClient;
import websocket.clients.impl.spring.SpringWebSocketClient;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

/**
 * @author Yuriy Tumakha
 */
@Component
@Slf4j
public class PerformanceBenchmark implements CommandLineRunner {

  private static final String SERVER_ENDPOINT = "wss://127.0.0.1:8883/websocket";
  private static final int MESSAGES_COUNT = 100_000;
  // private static final int MESSAGES_COUNT = 1_000_000;
  private static final String REPORT_FILENAME = "websocket-clients-performance.csv";
  private static final String HEADER = "WebSocket Client,Requests,Total duration,Time per request,Min,Avg,Max";
  private static final AtomicInteger I = new AtomicInteger();

  @Override
  public void run(String... args) {
    try {
      try (CsvWriter writer = new CsvWriter(REPORT_FILENAME, HEADER)) {
        testClient(new Java11WebSocketClient(), writer);
        testClient(new NettyWebSocketClient(), writer);
        testClient(new SpringWebSocketClient(), writer);
      }
    } catch (Exception e) {
      log.error("Test failed", e);
      System.exit(1);
    }
    System.exit(0);
  }

  private void testClient(WebSocketClient wsClient, CsvWriter reportWriter) throws Exception {
    System.out.println(I.incrementAndGet() + ". " + wsClient.getName());

    TimeStats stats = new ClientTestRun(wsClient, SERVER_ENDPOINT, MESSAGES_COUNT).run();

    reportWriter.println(format("%s,%d,%d,%d,%.3f,%.3f,%.3f",
        wsClient.getName(), MESSAGES_COUNT,
        stats.getTotalTime(), stats.getTimePerRequest(),
        stats.getMin(), stats.getAvg(), stats.getMax()));
  }

}
