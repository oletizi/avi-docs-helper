package com.avinetworks.docs.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class PermissiveSSLPageFetcher extends PageFetcher {
  public PermissiveSSLPageFetcher(final CrawlConfig config) {
    super(config);

    if (config.isIncludeHttpsPages()) {
      try {
        httpClient = HttpClients.custom().
            setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).
            setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
              public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                return true;
              }
            }).build()).build();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
