package websocket.server.ssl;

import lombok.SneakyThrows;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * SslContext to accept self-signed certificates on localhost.
 *
 * @author Yuriy Tumakha
 */
public abstract class SelfSignedSslContext {

    public static final SSLContext SSL_CONTEXT = insecureSSLContext();

    @SneakyThrows
    private static SSLContext insecureSSLContext() {
        // load up the key store
        String STORETYPE = "JKS";
        String KEYSTORE = "keystore.jks";
        String STOREPASSWORD = "storepassword";
        String KEYPASSWORD = "keypassword";

        KeyStore ks = KeyStore.getInstance(STORETYPE);
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(KEYSTORE);
        ks.load(inputStream, STOREPASSWORD.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, KEYPASSWORD.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext sslContext = null;
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }

}
