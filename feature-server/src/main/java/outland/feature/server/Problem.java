package outland.feature.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a Problem sent by the server as Problem JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Problem {

  private static final URI DEFAULT_TYPE = URI.create("about:blank");
  public static final URI AUTH_TYPE = URI.create("about:auth");
  public static final URI CLIENT_TYPE = URI.create("about:client");
  private static final URI ARG_TYPE = URI.create("about:argument");

  @JsonProperty("type")
  private URI type = DEFAULT_TYPE;

  @JsonProperty("title")
  private String title;

  @JsonProperty("status")
  private int status;

  @JsonProperty("detail")
  private String detail;

  @JsonProperty("instance")
  private URI instance;

  @JsonProperty("data")
  private Map data;

  private volatile transient String message;

  /**
   * Create a Problem object that indicates a client   error.
   *
   * @param title the problem title
   * @param detail the problem detail
   * @return a Problem object with a status of 401 and a type of "about:blank"
   */
  public static Problem clientProblem(String title, String detail, int code) {
    return new Problem()
        .title(title)
        .detail(detail)
        .data(Maps.newLinkedHashMap())
        .status(code)
        .type(CLIENT_TYPE);
  }

  /**
   * Create a Problem object that indicates an auth error.
   *
   * @param title the problem title
   * @param detail the problem detail
   * @return a Problem object with a status of 401 and a type of "about:blank"
   */
  public static Problem authProblem(String title, String detail) {
    return new Problem()
        .title(title)
        .detail(detail)
        .data(Maps.newLinkedHashMap())
        .status(401)
        .type(AUTH_TYPE);
  }

  /**
   * Create a Problem object that indicates an unspecified error.
   *
   * @param title the problem title
   * @param detail the problem detail
   * @return a Problem object with a status of 500 and a type of "about:blank"
   */
  public static Problem unspecifiedProblem(String title, String detail) {
    return new Problem()
        .title(title)
        .detail(detail)
        .data(Maps.newLinkedHashMap())
        .status(500)
        .type(DEFAULT_TYPE);
  }

  /**
   * Create a Problem object that indicates a bad argument.
   *
   * @param title the problem title
   * @param detail the problem detail
   * @return a Problem object with a status of 400 and a type of "about:argument"
   */
  public static Problem argProblem(String title, String detail) {
    return new Problem()
        .title(title)
        .detail(detail)
        .data(Maps.newLinkedHashMap())
        .status(400)
        .type(ARG_TYPE);
  }

  public Problem type(URI type) {
    this.type = type;
    return this;
  }

  public URI type() {
    return type;
  }

  public String title() {
    return title;
  }

  public Problem title(String title) {
    this.title = title;
    return this;
  }

  public int status() {
    return status;
  }

  public Problem status(int status) {
    this.status = status;
    return this;
  }

  public Optional<String> detail() {
    return Optional.ofNullable(detail);
  }

  public Problem detail(String detail) {
    this.detail = detail;
    return this;
  }

  public Optional<URI> instance() {
    return Optional.ofNullable(instance);
  }

  public Problem instance(URI instance) {
    this.instance = instance;
    return this;
  }

  public Map data() {
    return data;
  }

  public Problem data(Map data) {
    this.data = data;
    return this;
  }

  public String toMessage() {
    if (message != null) {
      return message;
    }
    message = title() + "; " + detail().orElse("") +
        " (" + status() + ")";
    return message;
  }

  @Override public int hashCode() {
    return Objects.hash(type, title, status, detail, instance, data);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Problem problem = (Problem) o;
    return status == problem.status &&
        Objects.equals(type, problem.type) &&
        Objects.equals(title, problem.title) &&
        Objects.equals(detail, problem.detail) &&
        Objects.equals(instance, problem.instance) &&
        Objects.equals(data, problem.data);
  }

  @Override public String toString() {
    return "Problem{" + "type=" + type +
        ", title='" + title + '\'' +
        ", status=" + status +
        ", detail='" + detail + '\'' +
        ", instance=" + instance +
        ", data=" + data +
        '}';
  }
}
