package outland.feature.server;

import java.security.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DnsCache {

  private static final Logger logger = LoggerFactory.getLogger(DnsCache.class);

  /**
   * Set the dns cache settings for the jvm on startup to stop caching names forever
   */
  static void setup() {
    change();
  }

  private static void change() {
    try {
      String nct = "networkaddress.cache.ttl";
      String ncnt = "networkaddress.cache.negative.ttl";
      logger.info("cache_dns pre {}: {}", nct, Security.getProperty(nct));
      logger.info("cache_dns pre {}: {}", ncnt, Security.getProperty(ncnt));
      Security.setProperty(nct, "3");
      Security.setProperty(ncnt, "3");
      logger.info("cache_dns post {}: {}", nct, Security.getProperty(nct));
      logger.info("cache_dns post {}: {}", ncnt, Security.getProperty(ncnt));
    } catch (SecurityException se) {
      throw new RuntimeException(se);
    }
  }
}
