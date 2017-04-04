package outland.feature.server;

import com.netflix.hystrix.exception.ExceptionNotWrappedByHystrix;

public class ServiceException extends RuntimeException implements ExceptionNotWrappedByHystrix {

  /**
   * Throw an IllegalArgumentException if the argument is null.
   *
   * @param arg the object to
   * @param message the exception message
   * @return the object if not null
   * @throws IllegalArgumentException if {@code obj} is {@code null}
   */
  public static <T> T throwIfNull(T arg, String message) {
    if (arg == null) {
      IllegalArgumentException cause = new IllegalArgumentException(message);
      throw new ServiceException(Problem.argProblem(cause.getMessage(), ""), cause);
    }
    return arg;
  }

  private Problem problem;

  /**
   * @param problem the Problem detail
   */
  public ServiceException(Problem problem) {
    super(problem.toMessage());
    this.problem = problem;
  }

  /**
   * @param problem the Problem detail
   * @param cause the cause
   */
  public ServiceException(Problem problem, Throwable cause) {
    super(problem.toMessage(), cause);
    this.problem = problem;
  }

  /**
   * @return the problem detail
   */
  public Problem problem() {
    return problem;
  }
}
