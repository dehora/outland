package outland.feature;

import org.junit.Test;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.NamespaceFeature;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FeatureRecordTest {

  @Test
  public void testPrepare() {
    final Feature feature = TestSupport.loadFeature("json/feature-1.json");
    FeatureRecord record = FeatureRecord.build(feature);

    assertEquals(feature, record.feature());

    final FeatureOption controlFeatureOption = record.controlFeatureOption();
    assertNotNull(controlFeatureOption);
    assertEquals("option-green", controlFeatureOption.getName());
    assertEquals("green", controlFeatureOption.getValue());
    assertEquals(3000, controlFeatureOption.getWeight());

    final NamespaceFeature staging = record.namespace("staging");
    assertNotNull(staging);

    final FeatureOption stagingControlOptionRecord = record.controlFeatureOption("staging");
    assertNotNull(stagingControlOptionRecord);

    FeatureOption stagingControlOptionData = null;
    final String control = staging.getFeature().getOptions().getControl();
    for (FeatureOption featureOption : staging.getFeature().getOptions().getItemsList()) {

      if (featureOption.getName().equals(control)) {
        stagingControlOptionData = featureOption;
        break;
      }
    }

    assertEquals(stagingControlOptionData, stagingControlOptionRecord);
  }
}