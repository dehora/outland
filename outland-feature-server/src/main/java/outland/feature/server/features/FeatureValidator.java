package outland.feature.server.features;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureData;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.NamespaceFeature;
import outland.feature.proto.NamespaceFeatureCollection;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.Owner;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;

class FeatureValidator {

  void validateFeatureUpdateThrowing(Feature feature) throws ServiceException {

    if(OptionType.flag != feature.getOptions().getOption()) {
      validateOptionsHaveIdsThrowing(feature);
    }
    validateKeysThrowing(feature);

    if (OptionType.flag != feature.getOptions().getOption()
        && feature.getOptions().getItemsCount() != 0) {
      // only validate options if they're sent and we're not a flag type
      validateOptionsThrowing(feature.getOptions());

      if(feature.hasNamespaced()) {
        validateNamespaceFeaturesThrowing(feature);
      }

    }
  }

  void validateFeatureRegistrationThrowing(Feature feature) throws ServiceException {

    validateOwnerThrowing(feature);
    validateKeysThrowing(feature);
    validateOptionsThrowing(feature.getOptions());
  }

  void validateOptionsThrowing(OptionCollection options) {
    if (OptionType.bool == options.getOption()) {
      validateBooleanOptionsThrowing(options);
    }

    if(OptionType.flag != options.getOption()) {
      validateWeightsThrowing(options);
    }
  }

  private void validateNamespaceFeaturesThrowing(Feature feature) {

    final List<NamespaceFeature> itemsList = feature.getNamespaced().getItemsList();
    for (NamespaceFeature namespaceFeature : itemsList) {

      if (!feature.getKey().equals(namespaceFeature.getFeature().getKey())) {
        throw new ServiceException(Problem.clientProblem("wrong_key_for_namespace_feature",
            "A namespace must have the same key as its feature name", 422));
      }

      if(namespaceFeature.getFeature().hasOptions()) {
        validateOptionsThrowing(namespaceFeature.getFeature().getOptions());
      }
    }
  }

  void validateBooleanOptionsThrowing(OptionCollection options) {

    if(options.getItemsCount() != 2) {
      throw new ServiceException(Problem.clientProblem("wrong_options_for_bool_feature",
          "A bool option must have two options", 422));
    }

    options.getItemsList().forEach(option -> {
      final String name = option.getName();
      if(!"true".equals(name) && !"false".equals(name)) {
        throw new ServiceException(Problem.clientProblem("wrong_name_for_bool_feature",
            "A bool option must have a name of 'true' or 'false'", 422));
      }

      final String value = option.getValue();
      if(!"true".equals(value) && !"false".equals(value)) {
        throw new ServiceException(Problem.clientProblem("wrong_value_for_bool_feature",
            "A bool option must have a value of 'true' or 'false'", 422));
      }

      if("true".equals(value) && ! "true".equals(name)) {
        throw new ServiceException(Problem.clientProblem("mismatched_name_value_for_bool_feature",
            "A bool option must have a value of 'true' and a name of 'true'", 422));
      }

      if("false".equals(value) && ! "false".equals(name)) {
        throw new ServiceException(Problem.clientProblem("mismatched_name_value_for_bool_feature",
            "A bool option must have a value of 'false' and a name of 'false'", 422));
      }

    });
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

  void validateFeatureDataKeysMatch(FeatureData existing, FeatureData update) {

    if(!existing.getKey().equals(update.getKey())) {
      throw new ServiceException(
          Problem.clientProblem("feature_keys_mismatch", "feature data keys must be the same",
              422));
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

    if(! firstSet.equals(secondSet)) {
      throw new ServiceException(
          Problem.clientProblem("option_ids_mismatch", "option ids must be the same",
              422));
    }
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
    if (! feature.hasOwner()) {
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
    feature.getOptions().getItemsList().forEach(option -> {
      validateFeatureKeyThrowing(option.getId(), Problem.clientProblem("missing_id_for_option",
          "A feature update must have ids for its options", 422));
    });
  }

  public void validateNamespaceFeatureCollection(NamespaceFeatureCollection updateNamespaced) {

  }

  public void validateFeatureDataNewCandidate(Feature existingFeature, NamespaceFeature incoming) {

  }
}
