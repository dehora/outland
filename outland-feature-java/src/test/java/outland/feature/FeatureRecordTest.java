package outland.feature;

import java.util.stream.IntStream;
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
  public void testEvaluateOptions() {

    // IntStream iterations are there to stress sampler

    FeatureRecord stringRecord = FeatureRecord.build(TestSupport.loadFeature(
        "json/feature-eval-string-off-default-on-namespace-off-namespace.json"));

    IntStream.range(0, 100).forEach(
        i -> {
          assertEquals("an off feature returns its control option",
              "green", stringRecord.evaluate());

          // production: off control: red, red's weight is 0 so can't be sampled
          assertEquals("an off namespace feature returns its control option",
              "red", stringRecord.evaluate("production"));

          // development: on control: blue, red weight :10000 => sample fixed to always be red
          assertEquals("an on namespace feature returns its weighted option",
              "red", stringRecord.evaluate("development"));
        });

    FeatureRecord boolRecord1 = FeatureRecord.build(TestSupport.loadFeature(
        "json/feature-eval-bool-off-default-on-namespace-always-true.json"));

    IntStream.range(0, 100).forEach(
        i -> {
          // weighted to 10K true so an eval can't return anything but true
          assertEquals("an off bool returns its control option",
              "false", boolRecord1.evaluate());

          // weighted to 10K true with a false control, so has to return true if on
          assertEquals("an on namespace feature returns its weighted option",
              "true", boolRecord1.evaluate("development"));
        });

    FeatureRecord boolRecord2 = FeatureRecord.build(TestSupport.loadFeature(
        "json/feature-eval-bool-on-default-off-namespace-always-false.json"));

    IntStream.range(0, 100).forEach(
        i -> {
          // false control weighted to 10K true so an eval can't return anything but true
          assertEquals("an on bool returns its weighted option",
              "true", boolRecord2.evaluate());

          /*
          this one's a bit strange. the namespace is off and the weights are 100% false
           but the control is true, so we expect the off to evaluate into "true"
            */
          assertEquals("an off namespace feature returns its control option, even if "
                  + "that control is 'true'",
              "true", boolRecord2.evaluate("development"));
        });
  }

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