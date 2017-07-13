package outland.feature;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import org.junit.Test;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureData;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.NamespaceFeature;
import outland.feature.proto.NamespaceFeatureCollection;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.Status;

import static junit.framework.TestCase.assertSame;

public class FeatureClientNamespaceTest {

  @Test
  public void testFlagWithNamespaceConfigured() {

    final String group = "the_app";
    final String namespace = "the_ns";
    final FeatureStoreTest featureStore = new FeatureStoreTest();
    final ArrayList<TestState> testStates = Lists.newArrayList(
        new TestState("the_feature_off_on", Status.off, Status.on, true,
            "expected on when default off and namespace on"),
        new TestState("the_feature_on_on", Status.on, Status.on, true,
            "expected on when default is on and namespace is on"),
        new TestState("the_feature_on_off", Status.on, Status.off, false,
            "expected off when default is on and namespace is off"),
        new TestState("the_feature_off_off", Status.off, Status.off, false,
            "expected off when default is off and namespace is off")
    );

    final FeatureClient client = prepareNamespacedClient(group, namespace, featureStore);

    for (TestState testState : testStates) {
      featureStore.put(prepareFeatureWithNamespace(
          group, namespace, testState.key, testState.defaultState, testState.namespaceState));
      assertSame(testState.msg, testState.expectedResult, client.enabled(testState.key));
    }
  }

  @Test
  public void testFlagWithNamespaceDefault() {

    final String group = "the_app";
    final String namespace = "the_ns";
    final FeatureStoreTest featureStore = new FeatureStoreTest();
    final ArrayList<TestState> testStates = Lists.newArrayList(
        new TestState("the_feature_off_on", Status.off, Status.on, false,
            "expected off when default is off and namespace is on"),
        new TestState("the_feature_on_on", Status.on, Status.on, true,
            "expected on when default is on and namespace is on"),
        new TestState("the_feature_on_off", Status.on, Status.off, true,
            "expected on when default is on and namespace is off"),
        new TestState("the_feature_off_off", Status.off, Status.off, false,
            "expected off when default is off and namespace is off")
    );

    final FeatureClient client = prepareDefaultClient(group, featureStore);

    for (TestState testState : testStates) {
      featureStore.put(prepareFeatureWithNamespace(
          group, namespace, testState.key, testState.defaultState, testState.namespaceState));
      assertSame(testState.msg, testState.expectedResult, client.enabled(testState.key));
    }
  }

  @Test
  public void testOptionWithNamespaceConfigured() {

    final String group = "the_app";
    final String namespace = "the_ns";
    final FeatureStoreTest featureStore = new FeatureStoreTest();
    final ArrayList<TestState> testStates = Lists.newArrayList(
        new TestState("the_feature_off_on", Status.off, Status.on, true,
            "expected on when default is off and namespace is on"),
        new TestState("the_feature_on_on", Status.on, Status.on, true,
            "expected on when default is on and namespace is on"),
        new TestState("the_feature_on_off", Status.on, Status.off, false,
            "expected off when default is on and namespace is off"),
        new TestState("the_feature_off_off", Status.off, Status.off, false,
            "expected off when default is off and namespace is off")
    );

    final FeatureClient client = prepareNamespacedClient(group, namespace, featureStore);

    // the weights in this tests are set to 10000 on and 0 off to disable bias
    for (TestState testState : testStates) {
      featureStore.put(prepareFeatureOptionWithNamespace(
          group, namespace, testState.key, testState.defaultState, testState.namespaceState));
      assertSame(testState.msg, testState.expectedResult, client.enabled(testState.key));
    }
  }

  @Test
  public void testOptionWithNamespaceDefault() {

    final String group = "the_app";
    final String namespace = "the_ns";
    final FeatureStoreTest featureStore = new FeatureStoreTest();
    final ArrayList<TestState> testStates = Lists.newArrayList(
        new TestState("the_feature_off_on", Status.off, Status.on, false,
            "expected off when default is off and namespace is on"),
        new TestState("the_feature_on_on", Status.on, Status.on, true,
            "expected on when default is on and namespace is on"),
        new TestState("the_feature_on_off", Status.on, Status.off, true,
            "expected on when default is on and namespace is off"),
        new TestState("the_feature_off_off", Status.off, Status.off, false,
            "expected off when default is off and namespace is off")
    );

    final FeatureClient client = prepareDefaultClient(group, featureStore);

    // the weights in this tests are set to 10000 on and 0 off to disable bias
    for (TestState testState : testStates) {
      featureStore.put(prepareFeatureOptionWithNamespace(
          group, namespace, testState.key, testState.defaultState, testState.namespaceState));
      assertSame(testState.msg, testState.expectedResult, client.enabled(testState.key));
    }
  }

  private FeatureClient prepareNamespacedClient(String group, String namespace,
      FeatureStoreTest featureStore) {
    final ServerConfiguration serverConfiguration = new ServerConfiguration()
        .baseURI("http://localhost")
        .defaultGroup(group)
        .namespace(namespace);

    return FeatureClient.newBuilder()
        .serverConfiguration(serverConfiguration)
        .featureStore(featureStore)
        .localFeatureStore(new FeatureStoreLocalNone())
        .build();
  }

  private FeatureClient prepareDefaultClient(String group, FeatureStoreTest featureStore) {
    final ServerConfiguration serverConfiguration = new ServerConfiguration()
        .baseURI("http://localhost")
        .defaultGroup(group);

    return FeatureClient.newBuilder()
        .serverConfiguration(serverConfiguration)
        .featureStore(featureStore)
        .localFeatureStore(new FeatureStoreLocalNone())
        .build();
  }

  private Feature prepareFeatureWithNamespace(String group, String namespace,
      String featureKey, Status defaultState, Status namespaceState) {
    final FeatureData.Builder fdBuilder =
        FeatureData.newBuilder()
            .setStatus(namespaceState);

    final NamespaceFeature.Builder nfBuilder = NamespaceFeature.newBuilder()
        .setNamespace(namespace)
        .setData(fdBuilder);

    final NamespaceFeatureCollection.Builder nfcBuilder = NamespaceFeatureCollection.newBuilder()
        .addItems(nfBuilder);

    final FeatureData.Builder fdBuilder1 =
        FeatureData.newBuilder()
            .setStatus(defaultState);

    final Feature.Builder featureBuilder = Feature.newBuilder();

    return featureBuilder.setGroup(group)
        .setKey(featureKey)
        .setData(fdBuilder1)
        .setNamespaces(nfcBuilder)
        .build();
  }

  private Feature prepareFeatureOptionWithNamespace(String group, String namespace,
      String featureKey, Status defaultState, Status namespaceState) {

    final FeatureOption.Builder foNamespaceTrueBuilder = FeatureOption.newBuilder()
        .setOption(OptionType.bool)
        .setValue("true")
        .setWeight(10_000);

    final FeatureOption.Builder foNamespaceFalseBuilder = FeatureOption.newBuilder()
        .setOption(OptionType.bool)
        .setValue("false")
        .setWeight(0);

    final OptionCollection.Builder ocNamespaceBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .setMaxweight(10_000)
        .addItems(foNamespaceTrueBuilder.buildPartial())
        .addItems(foNamespaceFalseBuilder.buildPartial());

    final FeatureData.Builder fdBuilder = FeatureData.newBuilder()
        .setStatus(namespaceState)
        .setOptions(ocNamespaceBuilder);

    final NamespaceFeature.Builder nfBuilder = NamespaceFeature.newBuilder()
        .setNamespace(namespace)
        .setData(fdBuilder);

    final NamespaceFeatureCollection.Builder nfcBuilder = NamespaceFeatureCollection.newBuilder()
        .addItems(nfBuilder);

    final FeatureOption.Builder foDefaultTrueBuilder = FeatureOption.newBuilder()
        .setOption(OptionType.bool)
        .setValue("true")
        .setWeight(10_000);

    final FeatureOption.Builder foDefaultFalseBuilder = FeatureOption.newBuilder()
        .setOption(OptionType.bool)
        .setValue("false")
        .setWeight(0);

    final OptionCollection.Builder ocDefaultBuilder = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .setMaxweight(10_000)
        .addItems(foDefaultTrueBuilder.buildPartial())
        .addItems(foDefaultFalseBuilder.buildPartial());

    final Feature.Builder featureBuilder = Feature.newBuilder();

    final FeatureData.Builder fdBuilder1 = FeatureData.newBuilder()
        .setStatus(defaultState)
        .setOptions(ocDefaultBuilder);

    return featureBuilder.setGroup(group)
        .setKey(featureKey)
        .setData(fdBuilder1)
        .setNamespaces(nfcBuilder)
        .build();
  }

  class TestState {

    final String key;
    final Status defaultState;
    final Status namespaceState;
    final boolean expectedResult;
    final String msg;

    TestState(String key, Status defaultState, Status namespaceState, boolean expectedResult,
        String msg) {
      this.key = key;
      this.defaultState = defaultState;
      this.namespaceState = namespaceState;
      this.expectedResult = expectedResult;
      this.msg = msg;
    }
  }
}
