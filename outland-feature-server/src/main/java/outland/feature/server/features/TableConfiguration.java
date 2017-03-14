package outland.feature.server.features;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class TableConfiguration {

  @NotEmpty
  @JsonProperty
  public String outlandFeaturesTable;

  @NotEmpty
  @JsonProperty
  public String outlandAppsTable;

  @NotEmpty
  @JsonProperty
  public String outlandAppGraphTable;

}
