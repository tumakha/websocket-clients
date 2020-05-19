package websocket.clients.impl.java11;

import lombok.SneakyThrows;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * SslContext to accept self-signed certificates on localhost.
 *
 * @author Yuriy Tumakha
 */
public abstract class InsecureSslContext {

  private static final X509Certificate[] EMPTY_X509_CERTIFICATES = {};

  private static final TrustManager ACCEPT_ALL_CERTIFICATES = new X509ExtendedTrustManager() {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String s) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String s) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return EMPTY_X509_CERTIFICATES;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {
    }

  };

  public static final SSLContext SSL_CONTEXT = insecureSSLContext();

  @SneakyThrows
  private static SSLContext insecureSSLContext() {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, new TrustManager[]{ACCEPT_ALL_CERTIFICATES}, new SecureRandom());

//    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
//    HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    return sslContext;
  }

}
