package outland.feature.server.features;

import com.google.protobuf.util.JsonFormat;
import org.junit.Test;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureData;
import outland.feature.proto.Status;

import static org.junit.Assert.*;

public class FeatureSupportTest {

  @Test
  public void testToFeature() throws Exception {

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setStatus(Status.off);

    Feature feature = Feature.newBuilder()
        .setId("id1")
        .setKey("key1")
        .setGroup("app1")
        .setDescription("desc1")
        .setData(featureData)
        .build();

    final String json = JsonFormat.printer().print(feature);

    final Feature feature1 = FeatureSupport.toFeature(json);

    assertEquals(feature.getId(), feature1.getId());
    assertEquals(feature.getKey(), feature1.getKey());
    assertEquals(feature.getGroup(), feature1.getGroup());
    assertEquals(feature.getDescription(), feature1.getDescription());
    assertEquals(feature.getData().getStatus(), feature1.getData().getStatus());

  }

}