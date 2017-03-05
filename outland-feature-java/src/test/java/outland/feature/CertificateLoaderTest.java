package outland.feature;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CertificateLoaderTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void build() {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    CertificateLoader securitySupport =
        new CertificateLoader("classpath:certs");

    try {
      securitySupport.applySslSocketFactory(builder);
      OkHttpClient build = builder.build();

      Request request = new Request.Builder()
          .url("https://helloworld.letsencrypt.org")
          .get()
          .build();

      build.newCall(request).execute();
    } catch (Exception e) {
      fail("expected custom letsencrypt install to succeed " + e.getMessage());
    }
  }

  @Test
  public void createSome() throws IOException {
    CertificateLoader securitySupport =
        new CertificateLoader("classpath:certs");

    X509TrustManager x509TrustManager = securitySupport.trustManager();
    SSLContext sslContext = securitySupport.sslContext();
    assertNotNull(x509TrustManager);
    assertNotNull(sslContext);
    X509Certificate[] acceptedIssuers = x509TrustManager.getAcceptedIssuers();

    assertEquals(3, acceptedIssuers.length);

    String issuer1 = "CN=Let's Encrypt Authority X1, O=Let's Encrypt, C=US";
    String issuer2 = "CN=Let's Encrypt Authority X2, O=Let's Encrypt, C=US";
    String issuer3 = "CN=Let's Encrypt Authority X3, O=Let's Encrypt, C=US";
    Set<String> seen = Sets.newHashSet();
    for (X509Certificate acceptedIssuer : acceptedIssuers) {
      String name = acceptedIssuer.getSubjectDN().getName();
      System.out.println(name);
      if (issuer1.equals(name)) {
        seen.add(name);
      }

      if (issuer2.equals(name)) {
        seen.add(name);
      }

      if (issuer3.equals(name)) {
        seen.add(name);
      }
    }

    assertEquals(3, seen.size());
    assertTrue(seen.contains(issuer1));
    assertTrue(seen.contains(issuer2));
    assertTrue(seen.contains(issuer3));
  }

  @Test
  public void createNone() throws IOException {
    String fPath = "file://" + folder.newFolder().getAbsolutePath();
    CertificateLoader securitySupport = new CertificateLoader(fPath);
    X509TrustManager x509TrustManager = securitySupport.trustManager();
    SSLContext sslContext = securitySupport.sslContext();

    assertNotNull(x509TrustManager);
    assertNotNull(sslContext);

    X509Certificate[] acceptedIssuers = x509TrustManager.getAcceptedIssuers();
    assertEquals(0, acceptedIssuers.length);
  }

  @Test
  public void createNull() throws IOException {
    CertificateLoader securitySupport = new CertificateLoader(null);
    X509TrustManager x509TrustManager = securitySupport.trustManager();
    SSLContext sslContext = securitySupport.sslContext();

    assertNull(x509TrustManager);
    assertNull(sslContext);
  }

  @Test
  public void resolvePath() throws IOException {
    try {
      CertificateLoader.resolvePath("classpath:cer");
      fail("exception expected for unknown classpath");
    } catch (Exception ignored) {
    }

    try {
      CertificateLoader.resolvePath("file://woo");
      fail("exception expected for file uri component");
    } catch (Exception ignored) {
    }

    try {
      CertificateLoader.resolvePath("file:///woo");
      fail("exception expected for missing path");
    } catch (Exception ignored) {
    }

    Path pathC = CertificateLoader.resolvePath("classpath:certs");
    assertTrue(pathC != null);

    String fPath = "file://" + folder.newFolder().getAbsolutePath();
    Path pathF = CertificateLoader.resolvePath(fPath);
    assertTrue(pathF != null);
  }
}