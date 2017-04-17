package outland.feature;

import com.google.common.base.Strings;

/**
 * The top level exception thrown by the client. All client exceptions extend this.
 */
public class FeatureException extends RuntimeException {

  /**
   * Throw a FeatureException if the argument is null.
   *
   * @param arg the object to check
   * @param message the exception message
   * @return the object if not null
   * @throws IllegalArgumentException if {@code obj} is {@code null}
   */
  static <T> T throwIfNull(T arg, String message) {
    if (arg == null) {
      IllegalArgumentException cause = new IllegalArgumentException(message);
      throw new FeatureException(Problem.configProblem(cause.getMessage(), ""), cause);
    }
    return arg;
  }

  /**
   * Throw a FeatureException if the argument is null or empty.
   *
   * @param arg the string to check
   * @param message the exception message
   * @return the string if not null
   * @throws IllegalArgumentException if {@code arg} is {@code null} or empty
   */
  static String throwIfNullOrEmpty(String arg, String message) {
    if (Strings.isNullOrEmpty(arg)) {
      IllegalArgumentException cause = new IllegalArgumentException(message);
      throw new FeatureException(Problem.configProblem(cause.getMessage(), ""), cause);
    }
    return arg;
  }

  private Problem problem;

  /**
   * @param problem the Problem detail
   */
  public FeatureException(Problem problem) {
    super(problem.toMessage());
    this.problem = problem;
  }

  /**
   * @param problem the Problem detail
   * @param cause the cause
   */
  public FeatureException(Problem problem, Throwable cause) {
    super(problem.toMessage(), cause);
    this.problem = problem;
  }

  /**
   * Object representation of an RFC7807 Problem. Used in the client to provide error details.
   *
   * @return the problem detail
   */
  public Problem problem() {
    return problem;
  }
}
