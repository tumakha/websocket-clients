package websocket.clients.benchmark;

import java.io.*;

/**
 * @author Yuriy Tumakha
 */
public class CsvWriter implements Closeable {

  private final PrintWriter reportWriter;

  public CsvWriter(String filename, String header) throws IOException {
    reportWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(filename))));
    reportWriter.println(header);
  }

  public void println(String line) {
    reportWriter.println(line);
    reportWriter.flush();
  }

  @Override
  public void close() {
    reportWriter.close();
  }

}
