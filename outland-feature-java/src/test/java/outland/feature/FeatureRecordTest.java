package outland.feature;

import org.junit.Test;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.NamespaceFeature;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;

public class FeatureRecordTest {

  @Test
  public void testEnabledFlag() {

    final String namespace = "staging";

    final Feature feature1 =
        TestSupport.loadFeature("json/feature-flag-on-default-off-namespace.json");
    FeatureRecord record1 = FeatureRecord.build(feature1);

    assertTrue("true when default state is on",
        record1.enabled());
    assertTrue("true when namespace missing and falls back to default",
        record1.enabled("missing"));
    assertFalse("false when an existing namespace state is off",
        record1.enabled(namespace));


    final Feature feature =
        TestSupport.loadFeature("json/feature-flag-off-default-on-namespace.json");
    FeatureRecord record = FeatureRecord.build(feature);

    assertFalse("false when default state is off",
        record.enabled());
    assertFalse("false when namespace missing and falls back to default",
        record.enabled("missing"));
    assertTrue("true when an existing namespace state is on",
        record.enabled(namespace));
  }

  @Test
  public void testEnabledString() {

    /*
     test state level enabled of a string option. when we're looking at strings wrt
     enabled, we're just looking at their on/states not evaluating them for a value.
    */

    final Feature feature =
        TestSupport.loadFeature("json/feature-string-off-default-on-namespace.json");
    FeatureRecord record = FeatureRecord.build(feature);
    final String namespace = "development";

    assertFalse("false when default state is off",
        record.enabled());
    assertFalse("false when namespace missing and falls back to default",
        record.enabled("missing"));
    assertTrue("true when an existing namespace state is on",
        record.enabled(namespace));
  }

  @Test
  public void testEnabledBool() {

    /*
     test state level enablement of a bool option. when we're looking at bools wrt
     enabled, we're just looking at their on/states, we're not  looking at their
     weighted values. factoring in weights values is done via the evaluation calls.
      */

    final Feature feature1 =
        TestSupport.loadFeature("json/feature-bool-off-default-on-namespace-always-false.json");
    FeatureRecord record1 = FeatureRecord.build(feature1);
    final String namespace = "development";

    assertFalse("false when default state is off",
        record1.enabled());
    assertFalse("false when namespace missing and falls back to default",
        record1.enabled("missing"));
    assertTrue("true when an existing namespace state is on but the weights 100% biased false",
        record1.enabled(namespace));


    final Feature feature =
        TestSupport.loadFeature("json/feature-bool-off-default-on-namespace-always-true.json");
    FeatureRecord record = FeatureRecord.build(feature);

    assertEquals(feature, record.feature());
    assertFalse("false when default state is off",
        record.enabled());
    assertFalse("false when namespace missing and falls back to default",
        record.enabled("missing"));
    assertTrue("true when an existing namespace state is on and the weight 100% biased true",
        record.enabled(namespace));
  }

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

    assertNotNull(record.optionEvaluatorWeighted());
    assertNotNull(record.optionEvaluatorWeighted("staging"));
  }
}