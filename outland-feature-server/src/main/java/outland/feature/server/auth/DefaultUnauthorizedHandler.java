package outland.feature.server.auth;

import io.dropwizard.auth.UnauthorizedHandler;
import java.util.concurrent.ThreadLocalRandom;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.server.Problem;

import static outland.feature.server.StructLog.kvp;

public class DefaultUnauthorizedHandler implements UnauthorizedHandler {
  private static final Logger logger = LoggerFactory.getLogger(DefaultUnauthorizedHandler.class);

  private static final String CHALLENGE_FORMAT = "%s realm=\"%s\"";

  @Override
  public Response buildResponse(String prefix, String realm) {

    final long grepCode = grepCode();
    final String formattedGrepCode = String.format("%016x", grepCode);

    logger.warn(kvp(
        "prefix", prefix,
        "realm", realm,
        "trace_id", formattedGrepCode,
        "err", "invalid_or_missing_credentials"));

    final Problem problem = Problem
        .authProblem("invalid_or_missing_credentials",
            kvp("prefix", prefix))
        .status(Response.Status.UNAUTHORIZED.getStatusCode());
    //noinspection unchecked
    problem.data().put("trace_id", String.format("%016x", grepCode));

    return Response.status(Response.Status.UNAUTHORIZED)
        .header(HttpHeaders.WWW_AUTHENTICATE, String.format(CHALLENGE_FORMAT, prefix, realm))
        .type(MediaType.APPLICATION_JSON)
        .entity(problem)
        .build();
  }

  private long grepCode() {
    return ThreadLocalRandom.current().nextLong();
  }
}
