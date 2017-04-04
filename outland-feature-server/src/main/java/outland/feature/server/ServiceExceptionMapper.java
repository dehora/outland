package outland.feature.server;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import io.dropwizard.auth.AuthenticationException;
import java.util.concurrent.ThreadLocalRandom;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Singleton
// HK2 doesn't resolve ServiceExceptionMapper<E extends ServiceException>
public class ServiceExceptionMapper implements ExceptionMapper<Throwable> {

  private static final Logger logger = LoggerFactory.getLogger(ServiceExceptionMapper.class);

  @Override public Response toResponse(Throwable t) {

    final long grepCode = grepCode();
    final String formattedGrepCode = String.format("%016x", grepCode);
    logger.error(t.getMessage() + " trace_id=" + formattedGrepCode, t);
    if(logger.isDebugEnabled()) {
      logger.error(t.getMessage() + " trace_id=" + formattedGrepCode, t);
    } else {
      logger.error(t.getMessage() + " trace_id=" + formattedGrepCode);
    }

    // unpack our underlying failure from hystrix and work with it directly
    if(HystrixRuntimeException.class.isAssignableFrom(t.getClass())) {
      t = t.getCause();
    }

    if (ServiceException.class.isAssignableFrom(t.getClass())) {

      ServiceException se = (ServiceException) t;
      final Problem problem = se.problem();

      //noinspection unchecked
      problem.data().put("trace_id", formattedGrepCode);

      return Response
          .status(problem.status())
          .type(MediaType.APPLICATION_JSON_TYPE)
          .entity(problem)
          .build();
    }

    if (AuthenticationException.class.isAssignableFrom(t.getClass())) {

      AuthenticationException ae = (AuthenticationException) t;
      final Problem problem = Problem.authProblem(ae.getMessage(), "").status(401);

      //noinspection unchecked
      problem.data().put("trace_id", formattedGrepCode);

      return Response
          .status(401)
          .type(MediaType.APPLICATION_JSON_TYPE)
          .entity(problem)
          .build();
    }

    if (WebApplicationException.class.isAssignableFrom(t.getClass())) {

      WebApplicationException wae = (WebApplicationException) t;
      final int status = wae.getResponse().getStatus();
      final Problem problem = Problem.unspecifiedProblem(wae.getMessage(), "").status(status);

      //noinspection unchecked
      problem.data().put("trace_id", formattedGrepCode);

      return Response
          .status(status)
          .type(MediaType.APPLICATION_JSON_TYPE)
          .entity(problem)
          .build();
    }

    final Problem problem = Problem.unspecifiedProblem("Unidentified error", t.getMessage());

    //noinspection unchecked
    problem.data().put("trace_id", formattedGrepCode);
    return Response
        .status(Response.Status.INTERNAL_SERVER_ERROR)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity(problem)
        .build();
  }

  private long grepCode() {
    return ThreadLocalRandom.current().nextLong();
  }
}
