package outland.feature.server.features;

import java.util.function.Consumer;
import org.junit.Test;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureData;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.Owner;
import outland.feature.server.ServiceException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class FeatureValidatorTest {

  @Test
  public void validateFeatureThrowingFlagOptionOk() {
    Feature.Builder builder = Feature.newBuilder();
    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1");

    try {
      new FeatureValidator().validateFeatureRegistrationThrowing(builder.build());
    } catch (ServiceException e) {
      fail();
    }
  }

  @Test
  public void validateFeatureThrowingStringOptionOk() {
    final Feature.Builder builder = Feature.newBuilder();

    final OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .addItems(FeatureOption.newBuilder().setValue("red").setKey("opt-red").setWeight(5000))
        .addItems(FeatureOption.newBuilder().setValue("green").setKey("opt-green").setWeight(3000))
        .addItems(FeatureOption.newBuilder().setValue("blue").setKey("opt-blue").setWeight(2000));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData);

    try {
      new FeatureValidator().validateFeatureRegistrationThrowing(builder.build());
    } catch (ServiceException e) {
      fail();
    }
  }

  @Test
  public void validateFeatureThrowingStringOptionUpdateOk() {
    final Feature.Builder builder = Feature.newBuilder();

    final OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .addItems(FeatureOption.newBuilder().setValue("red").setKey("opt-red").setWeight(5000).setId("r"))
        .addItems(FeatureOption.newBuilder().setValue("green").setKey("opt-green").setWeight(3000).setId("g"))
        .addItems(FeatureOption.newBuilder().setValue("blue").setKey("opt-blue").setWeight(2000).setId("b"));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
    ;

    try {
      new FeatureValidator().validateFeatureUpdateThrowing(builder.build());
    } catch (ServiceException e) {
      fail();
    }
  }

  @Test
  public void validateFeatureThrowingStringOptionCount() {
    final Feature.Builder builder = Feature.newBuilder();

    final OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.string);

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData);

    callValidate(builder, "insufficient_count_for_string_option_feature", 422);
  }

  @Test
  public void validateFeatureThrowingStringEmptyAndNullNames() {
    final Feature.Builder builder = Feature.newBuilder();

    final OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .addItems(FeatureOption.newBuilder().setValue("red").setKey("").setWeight(5000))
        .addItems(FeatureOption.newBuilder().setValue("green").setKey("opt-green").setWeight(3000))
        .addItems(FeatureOption.newBuilder().setValue("blue").setKey("opt-blue").setWeight(2000));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData);

    callValidate(builder, "empty_key_value_for_string_option_feature", 422);

    Feature.Builder builder1 = Feature.newBuilder();

    final OptionCollection.Builder collectionBuilder1 = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .addItems(FeatureOption.newBuilder().setValue("red").setWeight(5000))
        .addItems(FeatureOption.newBuilder().setValue("green").setKey("opt-green").setWeight(3000))
        .addItems(FeatureOption.newBuilder().setValue("blue").setKey("opt-blue").setWeight(2000));

    final FeatureData.Builder featureData1 = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder1.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData1);

    callValidate(builder, "empty_key_value_for_string_option_feature", 422);
  }

  @Test
  public void validateFeatureThrowingStringRepeatingNames() {
    final Feature.Builder builder = Feature.newBuilder();

    final OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .addItems(FeatureOption.newBuilder().setValue("red").setKey("opt-red").setWeight(5000))
        .addItems(FeatureOption.newBuilder().setValue("red").setKey("opt-red").setWeight(3000))
        .addItems(FeatureOption.newBuilder().setValue("blue").setKey("opt-blue").setWeight(2000));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData);

    callValidate(builder, "indistinct_key_value_for_string_option_feature", 422);
  }

  @Test
  public void validateFeatureThrowingStringOptionUpdateNoIds() {
    Feature.Builder builder = Feature.newBuilder();
    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .addItems(FeatureOption.newBuilder().setValue("red").setKey("opt-red").setWeight(5000))   // no id here
        .addItems(FeatureOption.newBuilder().setValue("green").setKey("opt-green").setWeight(3000).setId("g"))
        .addItems(FeatureOption.newBuilder().setValue("blue").setKey("opt-blue").setWeight(2000).setId("b"));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
    ;

    callValidateUpdate(builder, "missing_id_for_option", 422);
  }

  @Test
  public void validateFeatureThrowingStringWrongWeights() {
    Feature.Builder builder = Feature.newBuilder();

    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .addItems(FeatureOption.newBuilder().setValue("red").setKey("opt-red").setId("r"))
        .addItems(FeatureOption.newBuilder().setValue("green").setKey("opt-green").setId("g"))
        .addItems(FeatureOption.newBuilder().setValue("blue").setKey("opt-blue").setId("b"));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
    ;

    // weights are 0,  and 0

    callValidate(builder, "weights_wrong_total", 422);
  }

  @Test
  public void validateFeatureThrowingStringWeightsOutOfBounds() {
    Feature.Builder builder = Feature.newBuilder();

    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .addItems(FeatureOption.newBuilder().setValue("red").setKey("opt-red").setWeight(0).setId("r"))
        .addItems(FeatureOption.newBuilder().setValue("green").setKey("opt-green").setWeight(-1).setId("g"))
        .addItems(FeatureOption.newBuilder().setValue("blue").setKey("opt-blue").setWeight(0).setId("b"));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
    ;

    // weights are 0, 0 and -1

    callValidate(builder, "weights_out_of_bounds", 422);


    builder = Feature.newBuilder();
    collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .addItems(FeatureOption.newBuilder().setValue("red").setKey("opt-red").setWeight(0).setId("r"))
        .addItems(FeatureOption.newBuilder().setValue("green").setKey("opt-green").setWeight(10_001).setId("g"))
        .addItems(FeatureOption.newBuilder().setValue("blue").setKey("opt-blue").setWeight(0).setId("b"));

    final FeatureData.Builder featureData1 = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData1)
    ;

    // weights are 0, 0 and 10001

    callValidate(builder, "weights_out_of_bounds", 422);
  }

  @Test
  public void validateFeatureThrowingStringWeightsOutOfTotalBounds() {
    Feature.Builder builder = Feature.newBuilder();
    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .addItems(FeatureOption.newBuilder().setValue("red").setKey("opt-red").setWeight(3000).setId("r"))
        .addItems(FeatureOption.newBuilder().setValue("green").setKey("opt-green").setWeight(2000).setId("g"))
        .addItems(FeatureOption.newBuilder().setValue("blue").setKey("opt-blue").setWeight(5001).setId("b"));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
    ;

    // weights are 3000, 2000 and 5001

    callValidate(builder, "weights_wrong_total", 422);
  }

  @Test
  public void validateFeatureThrowingStringMismatchedOptionIds() {

    OptionCollection.Builder existing = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .addItems(FeatureOption.newBuilder().setValue("red").setKey("opt-red").setWeight(3000).setId("r"))
        .addItems(FeatureOption.newBuilder().setValue("green").setKey("opt-green").setWeight(2000).setId("g"))
        .addItems(FeatureOption.newBuilder().setValue("blue").setKey("opt-blue").setWeight(5000).setId("b"));


    OptionCollection.Builder update = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .addItems(FeatureOption.newBuilder().setValue("red").setKey("opt-red").setWeight(3000).setId("r"))
        .addItems(FeatureOption.newBuilder().setValue("green").setKey("opt-green").setWeight(2000).setId("g"))
        .addItems(FeatureOption.newBuilder().setValue("blue").setKey("opt-blue").setWeight(5000).setId("b"))
        // option id y is not already existing
        .addItems(FeatureOption.newBuilder().setValue("yello").setKey("opt-yello").setWeight(5000).setId("y"))
        ;


    try {
      new FeatureValidator().validateOptionIdsForUpdate(existing.build(), update.build());
      fail();
    } catch (ServiceException e) {
      assertEquals(422, e.problem().status());
      assertSame("option_ids_mismatch", e.problem().title());
    }
  }

  @Test
  public void validateFeatureThrowingStringNoUpdateOptionsOk() {

    OptionCollection.Builder existing = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .addItems(FeatureOption.newBuilder().setValue("red").setKey("opt-red").setWeight(3000).setId("r"))
        .addItems(FeatureOption.newBuilder().setValue("green").setKey("opt-green").setWeight(2000).setId("g"))
        .addItems(FeatureOption.newBuilder().setValue("blue").setKey("opt-blue").setWeight(5000).setId("b"));

    OptionCollection.Builder update = OptionCollection.newBuilder();

    try {
      new FeatureValidator().validateOptionIdsForUpdate(existing.build(), update.build());
    } catch (ServiceException e) {
      fail("an update without options should validate");
    }
  }

  @Test
  public void testControl() {

    OptionCollection.Builder strings = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .setControl("opt-gray")
        .addItems(FeatureOption.newBuilder().setValue("red").setKey("opt-red").setWeight(3000).setId("r"))
        .addItems(FeatureOption.newBuilder().setValue("green").setKey("opt-green").setWeight(2000).setId("g"))
        .addItems(FeatureOption.newBuilder().setValue("blue").setKey("opt-blue").setWeight(5000).setId("b"));

    try {
      new FeatureValidator().validateOptionsThrowing(strings.build());
      fail("options with an unknown control should fail");
    } catch (ServiceException e) {
      assertEquals(422, e.problem().status());
      assertSame("no_such_control_option", e.problem().title());
    }

    strings = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .setControl("opt-red")
        .addItems(FeatureOption.newBuilder().setValue("red").setKey("opt-red").setWeight(3000).setId("r"))
        .addItems(FeatureOption.newBuilder().setValue("green").setKey("opt-green").setWeight(2000).setId("g"))
        .addItems(FeatureOption.newBuilder().setValue("blue").setKey("opt-blue").setWeight(5000).setId("b"));

    try {
      new FeatureValidator().validateOptionsThrowing(strings.build());
    } catch (ServiceException e) {
      fail("string options with matching control should validate");
    }

    OptionCollection.Builder booleans = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .setControl("opt-gray")
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("true").setWeight(3000).setId("r"))
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("false").setWeight(7000).setId("g"))
        ;

    try {
      new FeatureValidator().validateOptionsThrowing(booleans.build());
      fail("options with an unknown control should fail");
    } catch (ServiceException e) {
      assertEquals(422, e.problem().status());
      assertSame("no_such_control_option", e.problem().title());
    }

    booleans = OptionCollection.newBuilder()
        .setOption(OptionType.string)
        .setControl("false")
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("true").setWeight(3000).setId("r"))
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("false").setWeight(7000).setId("g"))
    ;

    try {
      new FeatureValidator().validateOptionsThrowing(booleans.build());
    } catch (ServiceException e) {
      fail("boolean options with matching control should validate");
    }

  }

  @Test
  public void validateFeatureThrowingBoolOptionOk() {
    Feature.Builder builder = Feature.newBuilder();

    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("true").setWeight(5500))
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("false").setWeight(4500));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
    ;

    try {
      new FeatureValidator().validateFeatureRegistrationThrowing(builder.build());
    } catch (ServiceException e) {
      fail();
    }
  }

  @Test
  public void validateFeatureThrowingBoolOptionUpdateOk() {
    Feature.Builder builder = Feature.newBuilder();

    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("true").setWeight(5500).setId("a"))
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("false").setWeight(4500).setId("b"));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
    ;

    try {
      new FeatureValidator().validateFeatureUpdateThrowing(builder.build());
    } catch (ServiceException e) {
      fail();
    }
  }

  @Test
  public void validateFeatureThrowingNoOwner() {
    Feature.Builder builder = Feature.newBuilder();

    callValidate(builder, "no_owner_for_feature", 422);
  }

  @Test
  public void validateFeatureThrowingJunkOwner() {
    Feature.Builder builder = Feature.newBuilder();
    builder.setOwner(Owner.newBuilder().setName("Jayne"));

    callValidate(builder, "incomplete_owner", 422);
  }

  @Test
  public void validateFeatureThrowingNoKey() {
    Feature.Builder builder = Feature.newBuilder();
    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"));

    callValidate(builder, "no_key_for_feature", 422);
  }

  @Test
  public void validateFeatureThrowingNoAppKey() {
    Feature.Builder builder = Feature.newBuilder();
    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1");

    callValidate(builder, "no_group_for_feature", 422);
  }

  @Test
  public void validateFeatureThrowingBoolWrongOptions() {
    Feature.Builder builder = Feature.newBuilder();

    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("true"))
        ;

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
        ;

    callValidate(builder, "wrong_options_for_bool_feature", 422);
  }

  @Test
  public void validateFeatureThrowingBoolWrongNames() {
    Feature.Builder builder = Feature.newBuilder();

    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("true"))
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("alse"))
        ;

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
    ;

    callValidate(builder, "wrong_key_for_bool_feature", 422);
  }

  @Test
  public void validateFeatureThrowingBoolWrongValues() {
    Feature.Builder builder = Feature.newBuilder();

    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(FeatureOption.newBuilder().setValue("rue").setKey("true"))
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("false"));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
    ;

    callValidate(builder, "wrong_value_for_bool_feature", 422);
  }

  @Test
  public void validateFeatureThrowingBoolMismatchedValues() {
    Feature.Builder builder = Feature.newBuilder();

    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("false"))
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("false"));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
    ;

    callValidate(builder, "mismatched_key_value_for_bool_feature", 422);

    builder = Feature.newBuilder();

    collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("true"))
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("true"));

    final FeatureData.Builder featureData1 = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData1)
    ;

    callValidate(builder, "mismatched_key_value_for_bool_feature", 422);
  }

  @Test
  public void validateFeatureThrowingBoolWrongWeights() {
    Feature.Builder builder = Feature.newBuilder();

    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("true"))
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("false"));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
    ;

    // weights are 0 and 0

    callValidate(builder, "weights_wrong_total", 422);
  }

  @Test
  public void validateFeatureThrowingBoolWeightsOutOfBounds() {
    Feature.Builder builder = Feature.newBuilder();

    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("true"))
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("false").setWeight(-1));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
    ;

    // weights are 0 and -1

    callValidate(builder, "weights_out_of_bounds", 422);


    builder = Feature.newBuilder();
    collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("true"))
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("false").setWeight(10_001));

    final FeatureData.Builder featureData1 = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
    ;

    // weights are 0 and 10001

    callValidate(builder, "weights_out_of_bounds", 422);
  }

  @Test
  public void validateFeatureThrowingBoolWeightsOutOfTotalBounds() {
    Feature.Builder builder = Feature.newBuilder();
    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("true").setWeight(5000))
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("false").setWeight(5001));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
    ;

    // weights are 5000 and 5001

    callValidate(builder, "weights_wrong_total", 422);
  }

  @Test
  public void validateFeatureThrowingBoolOptionUpdateNoIds() {
    Feature.Builder builder = Feature.newBuilder();
    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("true").setWeight(5500))  // no id here
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("false").setWeight(5001).setId("b"));

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .setOptions(collectionBuilder);

    builder.setOwner(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setKey("key1")
        .setGroup("app1")
        .setData(featureData)
    ;

    callValidateUpdate(builder, "missing_id_for_option", 422);
  }

  @Test
  public void validateFeatureThrowingBoolNoUpdateOptionsOk() {

    OptionCollection.Builder existing = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("true"))
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("false"));

    OptionCollection.Builder update = OptionCollection.newBuilder();

    try {
      new FeatureValidator().validateOptionIdsForUpdate(existing.build(), update.build());
    } catch (ServiceException e) {
      fail("an update without options should validate");
    }
  }

  @Test
  public void validateFeatureThrowingBoolMismatchedOptionIds() {

    OptionCollection.Builder existing = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("true").setId("1"))
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("false").setId("2"));

    OptionCollection.Builder update = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(FeatureOption.newBuilder().setValue("true").setKey("true").setId("1"))
        // option id 3 is not already existing
        .addItems(FeatureOption.newBuilder().setValue("false").setKey("false").setId("3"));

    try {
      new FeatureValidator().validateOptionIdsForUpdate(existing.build(), update.build());
      fail();
    } catch (ServiceException e) {
      assertEquals(422, e.problem().status());
      assertSame("option_ids_mismatch", e.problem().title());
    }
  }

  private void callValidateUpdate(Feature.Builder builder, String expectedTitle, int expectedCode) {
    callValidator(builder, expectedTitle, expectedCode,
        feature -> new FeatureValidator().validateFeatureUpdateThrowing(feature));
  }

  private void callValidate(Feature.Builder builder, String expectedTitle, int expectedCode) {
    callValidator(builder, expectedTitle, expectedCode,
        feature -> new FeatureValidator().validateFeatureRegistrationThrowing(feature));
  }

  private void callValidator(Feature.Builder builder, String expectedTitle, int expectedCode,
      Consumer<Feature> v) {
    try {
      v.accept(builder.build());
      fail();
    } catch (ServiceException e) {
      assertEquals(expectedCode, e.problem().status());
      assertSame(expectedTitle, e.problem().title());
    }
  }
}