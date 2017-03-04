package outland.feature.server.resources;

import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;

public interface IdempotencyChecker {

  String REQ_HEADER = "Idempotency-Key";
  String RES_HEADER = "Idempotency-Key-Trace";

  default Optional<String> extractKey(HttpHeaders httpHeaders) {
    return Optional.ofNullable(httpHeaders.getHeaderString(IdempotencyChecker.REQ_HEADER));
  }

  boolean seen(String idempotencyKey);
}
