package outland.feature;

/**
 * The top level exception thrown by feature clients. All feature client API exceptions extend this
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
  public static <T> T throwIfNull(T arg, String message) {
    if (arg == null) {
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
   * @return the problem detail
   */
  public Problem problem() {
    return problem;
  }
}
