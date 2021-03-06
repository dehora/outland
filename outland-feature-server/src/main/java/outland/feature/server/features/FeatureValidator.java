package outland.feature.server.features;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureData;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.NamespaceFeature;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.Owner;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;

class FeatureValidator {

  void validateFeatureUpdateThrowing(Feature feature) throws ServiceException {

    if (feature.hasOwner()) {
      validateOwner(feature.getOwner());
    }

    validateKeysThrowing(feature);

    if (OptionType.flag != feature.getOptions().getOption()) {
      validateOptionsHaveIdsThrowing(feature);
    }

    if (OptionType.flag != feature.getOptions().getOption()
        && feature.getOptions().getItemsCount() != 0) {
      // only validate options if they're sent and we're not a flag type
      validateOptionsThrowing(feature.getOptions());

      if (feature.hasNamespaces()) {
        validateNamespaceFeaturesThrowing(feature);
      }
    }
  }

  void validateFeatureRegistrationThrowing(Feature feature) throws ServiceException {

    validateOwnerThrowing(feature);
    validateKeysThrowing(feature);
    validateOptionsThrowing(feature.getOptions());

    if (feature.hasNamespaces()) {
      validateNamespaceFeaturesThrowing(feature);
    }
  }

  void validateOwner(Owner owner) {
    if (Strings.isNullOrEmpty(owner.getEmail()) && Strings.isNullOrEmpty(
        owner.getUsername())) {
      throw new ServiceException(Problem.clientProblem("incomplete_owner",
          "An owner must have an email or a username", 422));
    }
  }

  void validateFeatureDataMergeCandidates(FeatureData existing, FeatureData incoming) {

    this.validateFeatureDataKeysMatch(existing, incoming);
    this.validateOptionsThrowing(incoming.getOptions());
    this.validateOptionIdsForUpdate(existing.getOptions(), incoming.getOptions());
  }

  void validateFeatureDataNewCandidateThrowing(Feature existingFeature, NamespaceFeature incoming) {

    validateKeysMatch(existingFeature.getKey(), incoming.getFeature().getKey());

    final OptionType existingOptionType = existingFeature.getOptions().getOption();

    final OptionCollection incomingOptions = incoming.getFeature().getOptions();
    final OptionType incomingOptionType = incomingOptions.getOption();

    if (!existingOptionType.equals(incomingOptionType)) {
      throw new ServiceException(
          Problem.clientProblem("namespace_feature_option_mismatch",
              "namespace feature option type must be the same as the parent feature's",
              422));
    }

    validateOptionsThrowing(incomingOptions);
  }

  void validateFeatureDataUpdateCandidateThrowing(
      Feature existingFeature, NamespaceFeature incoming) {

    final FeatureData incomingFeature = incoming.getFeature();

    boolean found = false;
    final List<String> namespaces = Lists.newArrayList();
    final List<NamespaceFeature> namespaceFeatures = existingFeature.getNamespaces().getItemsList();
    for (NamespaceFeature namespaceFeature : namespaceFeatures) {
      namespaces.add(namespaceFeature.getNamespace());
      if (namespaceFeature.getNamespace().equals(incoming.getNamespace())) {
        found = true;
        break;
      }
    }

    if (!found) {
      final String names = Joiner.on(", ").join(namespaces);
      throw new ServiceException(
          Problem.clientProblem("no_such_namespace",
              "the '" + incoming.getNamespace()
                  + "' namespace being updated was not found, known namespaces: "
                  + names,
              404));
    }

    namespaces.clear();

    validateKeysMatch(existingFeature.getKey(), incomingFeature.getKey());

    if (incoming.getFeature().hasOptions()) {
      final OptionType existingOptionType = existingFeature.getOptions().getOption();
      final OptionCollection incomingFeatureOptions = incomingFeature.getOptions();
      final OptionType incomingOptionType = incomingFeatureOptions.getOption();
      if (!existingOptionType.equals(incomingOptionType)) {
        throw new ServiceException(
            Problem.clientProblem("namespace_feature_option_mismatch",
                "namespace feature option type must be the same as the parent feature's",
                422));
      }

      if (OptionType.flag != incomingFeatureOptions.getOption()) {
        validateOptionsHaveIdsThrowing(incomingFeatureOptions);
        validateOptionIdsForUpdate(existingFeature.getOptions(), incomingFeatureOptions);
      }

      if (OptionType.flag != existingFeature.getOptions().getOption()
          && incomingFeatureOptions.getItemsCount() != 0) {
        // only validate options if they're sent and we're not a flag type
        validateOptionsThrowing(incomingFeatureOptions);
      }
    }
  }

  void validateOptionIdsForUpdate(OptionCollection existing, OptionCollection update) {

    if (update.getItemsCount() == 0) {
      // no options sent in update, skip
      return;
    }

    final Set<String> firstSet =
        existing.getItemsList().stream().map(FeatureOption::getId).collect(Collectors.toSet());
    final Set<String> secondSet =
        update.getItemsList().stream().map(FeatureOption::getId).collect(Collectors.toSet());

    if (!firstSet.equals(secondSet)) {
      throw new ServiceException(
          Problem.clientProblem("option_ids_mismatch", "option ids must be the same",
              422));
    }
  }

  void validateOptionsThrowing(OptionCollection options) {
    if (isBoolOption(options)) {
      validateBooleanOptionsThrowing(options);
    }

    if (isStringOption(options)) {
      validateStringOptionsThrowing(options);
    }

    if (OptionType.flag != options.getOption()) {
      validateWeightsThrowing(options);
    }
  }

  private void validateBooleanOptionsThrowing(OptionCollection options) {

    if (options.getItemsCount() != 2) {
      throw new ServiceException(Problem.clientProblem("wrong_options_for_bool_feature",
          "A bool option must have two options", 422));
    }

    options.getItemsList().forEach(option -> {
      final String key = option.getKey();
      if (!"true".equals(key) && !"false".equals(key)) {
        throw new ServiceException(Problem.clientProblem("wrong_key_for_bool_feature",
            "A bool option must have a key of 'true' or 'false'", 422));
      }

      final String value = option.getValue();
      if (!"true".equals(value) && !"false".equals(value)) {
        throw new ServiceException(Problem.clientProblem("wrong_value_for_bool_feature",
            "A bool option must have a value of 'true' or 'false'", 422));
      }

      if ("true".equals(value) && !"true".equals(key)) {
        throw new ServiceException(Problem.clientProblem("mismatched_key_value_for_bool_feature",
            "A bool option must have a value of 'true' and a key of 'true'", 422));
      }

      if ("false".equals(value) && !"false".equals(key)) {
        throw new ServiceException(Problem.clientProblem("mismatched_key_value_for_bool_feature",
            "A bool option must have a value of 'false' and a key of 'false'", 422));
      }
    });

    final String control = options.getControl();

    if (!Strings.isNullOrEmpty(control) && !("false".equals(control) || "true".equals(control))) {
      throw new ServiceException(
          Problem.clientProblem("no_such_control_option",
              "the '" + control + "' option key was not found in the options list.",
              422));
    }
  }

  private void validateFeatureDataKeysMatch(FeatureData existing, FeatureData update) {

    final String existingKey = existing.getKey();
    final String updateKey = update.getKey();

    validateKeysMatch(existingKey, updateKey);
  }

  private boolean isBoolOption(OptionCollection options) {
    return OptionType.bool == options.getOption();
  }

  private boolean isStringOption(OptionCollection options) {
    return OptionType.string == options.getOption();
  }

  private void validateNamespaceFeaturesThrowing(Feature feature) {

    final List<NamespaceFeature> itemsList = feature.getNamespaces().getItemsList();
    for (NamespaceFeature namespaceFeature : itemsList) {
      validateFeatureDataNewCandidateThrowing(feature, namespaceFeature);
    }
  }

  private void validateStringOptionsThrowing(OptionCollection options) {

    if (options.getItemsCount() == 0) {
      throw new ServiceException(
          Problem.clientProblem("insufficient_count_for_string_option_feature",
              "A string option must have at least one option", 422));
    }

    final List<FeatureOption> itemsList = options.getItemsList();

    final HashSet<String> optionKeys = Sets.newHashSetWithExpectedSize(itemsList.size());

    for (FeatureOption featureOption : itemsList) {
      if (Strings.isNullOrEmpty(featureOption.getKey())) {
        throw new ServiceException(
            Problem.clientProblem("empty_key_value_for_string_option_feature",
                "A string option's keys must be non-empty", 422));
      }

      optionKeys.add(featureOption.getKey());
    }

    if (itemsList.size() != optionKeys.size()) {
      throw new ServiceException(
          Problem.clientProblem("indistinct_key_value_for_string_option_feature",
              "A string option's keys must be distinct", 422));
    }

    final String control = options.getControl();

    if (!Strings.isNullOrEmpty(control) && !optionKeys.contains(control)) {
      throw new ServiceException(
          Problem.clientProblem("no_such_control_option",
              "the '" + control
                  + "' option key was not found in the options list, known keys: "
                  + Joiner.on(", ").join(optionKeys),
              422));
    }

    optionKeys.clear();
  }

  private void validateKeysThrowing(Feature feature) {
    validateFeatureKeyThrowing(feature.getKey(), Problem.clientProblem("no_key_for_feature",
        "A feature must have a key", 422));

    validateFeatureKeyThrowing(feature.getGroup(), Problem.clientProblem("no_group_for_feature",
        "A feature must have a group key", 422));
  }

  private void validateFeatureKeyThrowing(String key, Problem no_key_for_feature) {
    if (Strings.isNullOrEmpty(key)) {
      throw new ServiceException(no_key_for_feature);
    }
  }

  private void validateOwnerThrowing(Feature feature) {
    if (!feature.hasOwner()) {
      throw new ServiceException(Problem.clientProblem("no_owner_for_feature",
          "A feature must have an owner", 422));
    }

    final Owner owner = feature.getOwner();
    validateOwner(owner);
  }

  private void validateWeightsThrowing(OptionCollection options) {

    int sum = 0;
    for (FeatureOption option : options.getItemsList()) {
      if (option.getWeight() > 10_000 || option.getWeight() < 0) {
        throw new ServiceException(Problem.clientProblem("weights_out_of_bounds",
            "option weights must be between 0 and 10000", 422));
      }
      sum += option.getWeight();
    }

    if (sum != 10_000) {
      throw new ServiceException(
          Problem.clientProblem("weights_wrong_total", "option weights must sum to 10000",
              422));
    }
  }

  private void validateOptionsHaveIdsThrowing(Feature feature) {
    final OptionCollection options = feature.getOptions();

    validateOptionsHaveIdsThrowing(options);
  }

  private void validateOptionsHaveIdsThrowing(OptionCollection options) {
    options.getItemsList().forEach(option -> {
      validateFeatureKeyThrowing(option.getId(), Problem.clientProblem("missing_id_for_option",
          "A feature update must have ids for its options", 422));
    });
  }

  private void validateKeysMatch(String existingFeatureKey, String incomingKey) {
    if (!existingFeatureKey.equals(incomingKey)) {
      throw new ServiceException(
          Problem.clientProblem("namespace_feature_keys_mismatch",
              "namespace feature keys must be the same as the parent feature's",
              422));
    }
  }
}
