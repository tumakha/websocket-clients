package websocket.clients.benchmark;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import websocket.clients.WebSocketClient;
import websocket.clients.benchmark.model.TimeStats;
import websocket.clients.impl.akka.AkkaWebSocketClient;
import websocket.clients.impl.asynchttpclient.AsyncHttpWebSocketClient;
import websocket.clients.impl.java11.Java11WebSocketClient;
import websocket.clients.impl.javawebsocket.JavaWebSocketClient;
import websocket.clients.impl.jetty.JettyWebSocketClient;
import websocket.clients.impl.netty.NettyWebSocketClient;
import websocket.clients.impl.nv.NeoVisionariesWebSocketClient;
import websocket.clients.impl.spring.SpringReactorWebSocketClient;
import websocket.clients.impl.spring.SpringStandardWebSocketClient;

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
  //private static final int MESSAGES_COUNT = 1_000_000;
  private static final String REPORT_FILENAME = "websocket-clients-performance.csv";
  private static final String HEADER = "WebSocket Client,Messages,Total duration,Time per message,Min,Avg,Max,90%,99%,99.9%";
  private static final AtomicInteger I = new AtomicInteger();

  private static final String GREEN_CONSOLE = "\u001b[32;1m";
  private static final String RESET_CONSOLE = "\u001b[0m";

  @Override
  public void run(String... args) {
    warmUp(new Java11WebSocketClient());

    try {
      try (CsvWriter writer = new CsvWriter(REPORT_FILENAME, HEADER)) {
        testClient(new Java11WebSocketClient(), writer);
        testClient(new NettyWebSocketClient(), writer);
        testClient(new SpringReactorWebSocketClient(), writer);
        testClient(new SpringStandardWebSocketClient(), writer);
        testClient(new JettyWebSocketClient(), writer);
        testClient(new AkkaWebSocketClient(), writer);
        testClient(new AsyncHttpWebSocketClient(), writer);
        testClient(new JavaWebSocketClient(), writer);
        testClient(new NeoVisionariesWebSocketClient(), writer);
      }
    } catch (Exception e) {
      log.error("Test failed", e);
      System.exit(1);
    }
    System.exit(0);
  }

  @SneakyThrows
  private void warmUp(WebSocketClient wsClient) {
    System.out.println("Warming up");

    new ClientTestRun(wsClient, SERVER_ENDPOINT, MESSAGES_COUNT).run();
  }

  private void testClient(WebSocketClient wsClient, CsvWriter reportWriter) throws Exception {
    System.out.println("\n" + GREEN_CONSOLE + I.incrementAndGet() + ". " + wsClient.getName() + RESET_CONSOLE);

    TimeStats stats = new ClientTestRun(wsClient, SERVER_ENDPOINT, MESSAGES_COUNT).run();

    reportWriter.println(format("%s,%d,%d,%d,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f",
        wsClient.getName(), MESSAGES_COUNT,
        stats.getTotalTime(), stats.getTimePerRequest(),
        stats.getMin(), stats.getAvg(), stats.getMax(),
        stats.getMax90(), stats.getMax99(), stats.getMax99dot9()));
  }

}
