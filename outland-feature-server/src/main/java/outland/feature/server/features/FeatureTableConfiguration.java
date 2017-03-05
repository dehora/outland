package outland.feature.server.features;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class FeatureTableConfiguration {

  @NotEmpty
  @JsonProperty
  public String outlandFeaturesTable;

}
