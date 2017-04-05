package outland.feature.server.apps;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.protobuf.TextFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.AccessCollection;
import outland.feature.proto.Namespace;
import outland.feature.proto.MemberAccess;
import outland.feature.proto.Owner;
import outland.feature.proto.OwnerCollection;
import outland.feature.proto.ServiceAccess;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;
import outland.feature.server.features.MetricsTimer;
import outland.feature.server.features.Ulid;
import outland.feature.server.features.VersionService;

import static outland.feature.server.StructLog.kvp;

public class DefaultNamespaceService implements NamespaceService, MetricsTimer {

  private static final Logger logger = LoggerFactory.getLogger(DefaultNamespaceService.class);
  public static final String SUBJECT_TYPE = "app";

  private final NamespaceStorage namespaceStorage;
  private final VersionService versionService;
  private final AppUpdateProcessor appUpdateProcessor;

  private Timer saveAppTimer;
  private Timer saveServiceTimer;
  private Timer saveOwnerTimer;
  private Timer saveMemberTimer;
  private Timer readNamespaceTimer;
  private Timer removeRelationTimer;

  @Inject
  public DefaultNamespaceService(
      NamespaceStorage namespaceStorage,
      VersionService versionService,
      MetricRegistry metrics
  ) {
    this.namespaceStorage = namespaceStorage;
    this.versionService = versionService;
    this.appUpdateProcessor = new AppUpdateProcessor();
    configureMetrics(metrics);
  }

  @Override public Optional<Namespace> registerNamespace(Namespace namespace) {
    logger.info("{} /namespace[{}]", kvp("op", "registerNamespace"), TextFormat.shortDebugString(namespace));
    new NamespaceValidator().validateNamespaceRegistrationThrowing(namespace);
    return processRegistration(namespace);
  }

  @Override public Namespace updateNamespace(Namespace namespace) {
    logger.info("{} /namespace[{}]", kvp("op", "updateNamespace"), TextFormat.shortDebugString(namespace));

    return processUpdate(namespace, builder -> {});
  }

  @Override public Namespace addToNamespace(Namespace namespace, ServiceAccess service) {
    logger.info("{} /namespace[{}]/svc[{}]", kvp("op", "addToNamespace.service"),
        TextFormat.shortDebugString(namespace), TextFormat.shortDebugString(service));

    return processUpdate(namespace,
        builder -> {
          AccessCollection.Builder accessBuilder = newGrantCollectionBuilder();
          accessBuilder.addAllServices(appUpdateProcessor.mergeServices(namespace, service));
          accessBuilder.addAllMembers(namespace.getGranted().getMembersList());
          builder.setGranted(accessBuilder.buildPartial());
        });
  }

  @Override public Namespace addToNamespace(Namespace namespace, MemberAccess member) {
    logger.info("{} /namespace[{}]/mbr[{}]", kvp("op", "addToNamespace.member"),
        TextFormat.shortDebugString(namespace), TextFormat.shortDebugString(member));

    return processUpdate(namespace,
        builder -> {
          AccessCollection.Builder accessBuilder = newGrantCollectionBuilder();
          accessBuilder.addAllMembers(appUpdateProcessor.mergeMembers(namespace, member));
          accessBuilder.addAllServices(namespace.getGranted().getServicesList());
          builder.setGranted(accessBuilder.buildPartial());
        });
  }

  @Override public Namespace addToNamespace(Namespace namespace, final Owner incoming) {
    logger.info("{} /namespace[{}]/own[{}]", kvp("op", "addToNamespace.owner"),
        TextFormat.shortDebugString(namespace), TextFormat.shortDebugString(incoming));

    return processUpdate(namespace,
        builder -> builder.setOwners(OwnerCollection.newBuilder()
            .addAllItems(appUpdateProcessor.mergeOwners(namespace, incoming))));
  }

  @Override public Namespace removeServiceAccess(Namespace namespace, String serviceKey) {

    if(serviceKey == null) {
      return  namespace;
    }

    final List<ServiceAccess> servicesList = namespace.getGranted().getServicesList();
    final ArrayList<ServiceAccess> wrapped = Lists.newArrayList(servicesList);
    final Iterator<ServiceAccess> iterator = wrapped.iterator();
    ServiceAccess service = null;
    while (iterator.hasNext()) {
      final ServiceAccess next = iterator.next();
      if (serviceKey.equals(next.getKey())) {
        service = next;
        iterator.remove();
        break;
      }
    }

    if(service == null) {
      return namespace;
    }

    final Namespace.Builder builder = namespace.toBuilder();
    builder.clearGranted();

    AccessCollection.Builder newBuilder = newGrantCollectionBuilder();
    newBuilder.addAllServices(wrapped);
    newBuilder.addAllMembers(namespace.getGranted().getMembersList());

    builder.setGranted(newBuilder.buildPartial());

    OffsetDateTime now = OffsetDateTime.now();
    String updateTime = NamespaceService.asString(now);
    builder.setUpdated(updateTime);

    final Namespace updated = builder.build();
    updateAppInner(updated);
    removeServiceFromGraph(updated, service);
    return updated;
  }

  @Override public Namespace removeMemberAccess(Namespace namespace, String memberKey) {
    if(memberKey == null) {
      return  namespace;
    }

    final List<MemberAccess> ownersList = namespace.getGranted().getMembersList();
    final ArrayList<MemberAccess> wrapped = Lists.newArrayList(ownersList);
    final Iterator<MemberAccess> iterator = wrapped.iterator();
    MemberAccess MemberAccess = null;

    while (iterator.hasNext()) {
      MemberAccess next = iterator.next();
      if(memberKey.equals(next.getUsername())) {
        MemberAccess  = next;
        iterator.remove();
        break;
      }

      if(memberKey.equals(next.getEmail())) {
        MemberAccess  = next;
        iterator.remove();
        break;
      }
    }

    if(MemberAccess == null) {
      return namespace;
    }

    final Namespace.Builder builder = namespace.toBuilder();
    builder.clearGranted();
    AccessCollection.Builder newBuilder = newGrantCollectionBuilder();
    newBuilder.addAllMembers(wrapped);
    newBuilder.addAllServices(namespace.getGranted().getServicesList());

    builder.setGranted(newBuilder.buildPartial());

    OffsetDateTime now = OffsetDateTime.now();
    String updateTime = NamespaceService.asString(now);
    builder.setUpdated(updateTime);

    final Namespace updated = builder.build();
    updateAppInner(updated);
    removeMemberFromGraph(updated, MemberAccess);
    return updated;

  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Override public Namespace removeOwner(Namespace namespace, String ownerKey) {

    if(ownerKey == null) {
      return namespace;
    }

    if(namespace.getOwners().getItemsCount() == 1) {
      throw new ServiceException(Problem.clientProblem("at_least_one_owner",
          "only one owner remaining, refusing to remove", 422));
    }

    final List<Owner> ownersList = namespace.getOwners().getItemsList();
    final ArrayList<Owner> wrapped = Lists.newArrayList(ownersList);
    final Iterator<Owner> iterator = wrapped.iterator();
    Owner owner = null;

    while (iterator.hasNext()) {
      Owner next = iterator.next();
      if(ownerKey.equals(next.getUsername())) {
        owner  = next;
        iterator.remove();
        break;
      }

      if(ownerKey.equals(next.getEmail())) {
        owner  = next;
        iterator.remove();
        break;
      }
    }

    if(owner == null) {
      return namespace;
    }

    final Namespace.Builder builder = namespace.toBuilder();
    builder.getOwners().getItemsList().clear();
    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType("owner.collection")
        .addAllItems(wrapped);
    builder.setOwners(oc);

    OffsetDateTime now = OffsetDateTime.now();
    String updateTime = NamespaceService.asString(now);
    builder.setUpdated(updateTime);

    final Namespace updated = builder.build();
    updateAppInner(updated);
    removeOwnerFromGraph(updated, owner);
    return updated;
  }

  @Override public boolean hasOwner(String nsKey, String usernameOrEmail) {
    return appHasOwnerRelation(nsKey, NamespaceService.OWNER, usernameOrEmail);
  }

  @Override public boolean hasServiceAccess(String nsKey, String serviceKey) {
    return appHasGrantRelation(nsKey, NamespaceService.SERVICE, serviceKey);
  }

  @Override public boolean hasMemberAccess(String nsKey, String usernameOrEmail) {
    return appHasGrantRelation(nsKey, NamespaceService.MEMBER, usernameOrEmail);
  }

  @Override public Optional<Namespace> loadNamespaceByKey(String nsKey) {
    return timed(readNamespaceTimer, () -> namespaceStorage.loadNamespaceByKey(nsKey));
  }

  private Owner prepareOwner(Owner owner) {
    return appUpdateProcessor.prepareOwner(owner);
  }

  private ServiceAccess prepareService(ServiceAccess service) {
    return appUpdateProcessor.prepareService(service);
  }

  private MemberAccess prepareMember(MemberAccess member) {
    return appUpdateProcessor.prepareMember(member);
  }

  private Namespace processUpdate(Namespace namespace, Consumer<Namespace.Builder> updateExtra) {
    final Namespace.Builder builder = newAppBuilder(namespace);
    builder.setUpdated(NamespaceService.asString(OffsetDateTime.now()));
    updateExtra.accept(builder);
    final Namespace updated = builder.build();
    updateAppInner(updated);
    updateOwners(updated);
    updateMembers(updated);
    updateServices(updated);
    return updated;
  }

  private Optional<Namespace> processRegistration(Namespace namespace) {
    OffsetDateTime now = OffsetDateTime.now();
    String created = NamespaceService.asString(now);
    final Namespace.Builder appBuilder = newAppBuilder(namespace);
    appBuilder.setId("app_" + Ulid.random(now.toInstant().toEpochMilli()));
    appBuilder.setCreated(created);
    appBuilder.setUpdated(created);

    List<Owner> ownersReady = Lists.newArrayList();
    namespace.getOwners().getItemsList().forEach(owner -> ownersReady.add(prepareOwner(owner)));
    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType("owner.collection")
        .addAllItems(ownersReady);
    appBuilder.setOwners(oc);

    AccessCollection.Builder accessCollectionBuilder = newGrantCollectionBuilder();

    List<ServiceAccess> servicesReady = Lists.newArrayList();
    namespace.getGranted().getServicesList().forEach(service -> servicesReady.add(prepareService(service)));
    accessCollectionBuilder.addAllServices(servicesReady);

    List<MemberAccess> memberReady = Lists.newArrayList();
    namespace.getGranted().getMembersList().forEach(access -> memberReady.add(prepareMember(access)));
    accessCollectionBuilder.addAllMembers(memberReady);

    appBuilder.clearGranted();
    appBuilder.setGranted(accessCollectionBuilder.buildPartial());

    final Namespace registered = appBuilder.build();

    // todo: the usual compensating write failure stuff
    registerAppInner(registered);
    registerOwners(registered);
    registerServices(registered);
    registerMembers(registered);

    return Optional.of(registered);
  }

  private AccessCollection.Builder newGrantCollectionBuilder() {
    return AccessCollection.newBuilder().setType("access.collection");
  }

  private Namespace.Builder newAppBuilder(Namespace namespace) {
    return namespace.toBuilder().setType(DefaultNamespaceService.SUBJECT_TYPE);
  }

  private boolean appHasOwnerRelation(String appKey, String relatedType, String relatedKey) {
    return appHasRelation(appKey, relatedType, relatedKey, OWNER_RELATION);
  }

  private boolean appHasGrantRelation(String appKey, String relatedType, String relatedKey) {
    return appHasRelation(appKey, relatedType, relatedKey, ACCESS_RELATION);
  }

  private boolean appHasRelation(String appKey, String relatedType, String relatedKey,
      String relation) {
    final String subjectType = DefaultNamespaceService.SUBJECT_TYPE;
    final String subjectKey = appKey;
    final String objectType = relatedType;
    final String objectKey = relatedKey;
    final String inv = "inv.";

    final String inverseRelationHashKey = objectType + "." + objectKey;
    final String inverseRelationRangeKey = inv + relation + "." + subjectType + "." + subjectKey;

    return namespaceStorage.queryRelationExists(inverseRelationHashKey, inverseRelationRangeKey);
  }

  private void registerAppInner(Namespace registered) {
    timed(saveAppTimer, () -> namespaceStorage.createNamespace(registered));
  }

  private void registerServices(Namespace namespace) {
    namespace.getGranted().getServicesList().forEach(service -> addServiceToGraph(namespace, service));
  }

  private void registerMembers(Namespace namespace) {
    namespace.getGranted().getMembersList().forEach(member -> addMemberToGraph(namespace, member));
  }

  private void registerOwners(Namespace namespace) {
    namespace.getOwners().getItemsList().forEach(service -> addOwnerToGraph(namespace, service));
  }

  private void updateAppInner(Namespace registered) {
    timed(saveAppTimer, () -> namespaceStorage.saveNamespace(registered));
  }

  private void updateServices(Namespace namespace) {
    namespace.getGranted().getServicesList().forEach(service -> addServiceToGraph(namespace, service));
  }

  private void updateMembers(Namespace namespace) {
    namespace.getGranted().getMembersList().forEach(member -> addMemberToGraph(namespace, member));
  }

  private void updateOwners(Namespace namespace) {
    namespace.getOwners().getItemsList().forEach(service -> addOwnerToGraph(namespace, service));
  }

  private void addServiceToGraph(Namespace namespace, ServiceAccess service) {

    final String relation = ACCESS_RELATION;
    final String subjectType = DefaultNamespaceService.SUBJECT_TYPE;
    final String subjectKey = namespace.getKey();
    final String objectType = NamespaceService.SERVICE;
    final String objectKey = service.getKey();

    saveGraphRelation(
        namespace, relation, subjectType, subjectKey, objectType, objectKey, saveServiceTimer);
  }

  private void addMemberToGraph(Namespace namespace, MemberAccess member) {
    final String relation = ACCESS_RELATION;
    final String subjectType = DefaultNamespaceService.SUBJECT_TYPE;
    final String subjectKey = namespace.getKey();
    final String objectType = NamespaceService.MEMBER;

    if(! Strings.isNullOrEmpty(member.getUsername())) {
      final String objectKey = member.getUsername();
      saveGraphRelation(
          namespace, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }

    if(! Strings.isNullOrEmpty(member.getEmail())) {
      final String objectKey = member.getEmail();
      saveGraphRelation(
          namespace, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }
  }

  private void addOwnerToGraph(Namespace namespace, Owner owner) {
    final String relation = OWNER_RELATION;
    final String subjectType = DefaultNamespaceService.SUBJECT_TYPE;
    final String subjectKey = namespace.getKey();
    final String objectType = NamespaceService.OWNER;

    if(! Strings.isNullOrEmpty(owner.getUsername())) {
      final String objectKey = owner.getUsername();
      saveGraphRelation(
          namespace, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }

    if(! Strings.isNullOrEmpty(owner.getEmail())) {
      final String objectKey = owner.getEmail();
      saveGraphRelation(
          namespace, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }

  }

  private void saveGraphRelation(
      Namespace namespace,
      String relation,
      String subjectType,
      String subjectKey,
      String objectType,
      String objectKey,
      Timer timer
  ) {
    /*
      Store two records, one subject to object relation, the other an inverse
      object to subject relation. The two are marked as "rel" and "inv" as that
      allows us to have one relationship term (eg "member") without defining
      an inverse term. The records' range keys also have a type; this
      allows queries about a relation in general, but also about a relation
      between a subject and a specific type (range queries in dynamo can use a
      prefix pattern). In our case we can use it to track the owner members of an
      namespace distinctly from the service members of an namespace, or just grab both membership
      kinds.

      +-----------------------------+------------------------------------------+
      | hash                        |   range                                  |
      +-----------------------------+------------------------------------------+
      | $subject_type.$subject_key  |   rel.$relation.$object_type.$object_key |
      +-----------------------------+------------------------------------------+
      | $object_type.$object_key    |   inv.$relation.$object_type.$object_key |
      +-----------------------------+------------------------------------------+
     */

    final String rel = "rel.";
    final String inv = "inv.";

    final String relationHashKey = subjectType + "." + subjectKey;
    final String relationRangeKey = rel + relation + "." + objectType + "." + objectKey;
    timed(timer,
        () -> namespaceStorage.saveRelation(namespace, relationHashKey, relationRangeKey));

    final String inverseRelationHashKey = objectType + "." + objectKey;
    final String inverseRelationRangeKey = inv + relation + "." + subjectType + "." + subjectKey;
    timed(timer,
        () -> namespaceStorage.saveRelation(namespace, inverseRelationHashKey, inverseRelationRangeKey));
  }

  private void removeOwnerFromGraph(Namespace namespace, Owner owner) {
    final String relation = OWNER_RELATION;
    final String subjectType = SUBJECT_TYPE;
    final String subjectKey = namespace.getKey();
    final String objectType = NamespaceService.OWNER;

    if(! Strings.isNullOrEmpty(owner.getUsername())) {
      final String objectKey = owner.getUsername();
      removeGraphRelation(
          namespace, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }

    if(! Strings.isNullOrEmpty(owner.getEmail())) {
      final String objectKey = owner.getEmail();
      removeGraphRelation(
          namespace, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }
  }

  private void removeMemberFromGraph(Namespace namespace, MemberAccess member) {
    final String relation = ACCESS_RELATION;
    final String subjectType = SUBJECT_TYPE;
    final String subjectKey = namespace.getKey();
    final String objectType = NamespaceService.MEMBER;

    if(! Strings.isNullOrEmpty(member.getUsername())) {
      final String objectKey = member.getUsername();
      removeGraphRelation(
          namespace, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }

    if(! Strings.isNullOrEmpty(member.getEmail())) {
      final String objectKey = member.getEmail();
      removeGraphRelation(
          namespace, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }
  }

  private void removeServiceFromGraph(Namespace namespace, ServiceAccess service) {
    final String relation = ACCESS_RELATION;
    final String subjectType = SUBJECT_TYPE;
    final String subjectKey = namespace.getKey();
    final String objectType = NamespaceService.SERVICE;
    final String objectKey = service.getKey();

    removeGraphRelation(
        namespace, relation, subjectType, subjectKey, objectType, objectKey, removeRelationTimer);
  }

  private void removeGraphRelation(
      Namespace namespace,
      String relation,
      String subjectType,
      String subjectKey,
      String objectType,
      String objectKey,
      Timer timer
  ) {
    final String rel = "rel.";
    final String inv = "inv.";

    final String relationHashKey = subjectType + "." + subjectKey;
    final String relationRangeKey = rel + relation + "." + objectType + "." + objectKey;
    timed(timer,
        () -> namespaceStorage.removeRelation(namespace, relationHashKey, relationRangeKey));

    final String inverseRelationHashKey = objectType + "." + objectKey;
    final String inverseRelationRangeKey = inv + relation + "." + subjectType + "." + subjectKey;
    timed(timer,
        () -> namespaceStorage.removeRelation(namespace, inverseRelationHashKey, inverseRelationRangeKey));
  }

  private void configureMetrics(MetricRegistry metrics) {
    saveAppTimer = metrics.timer(MetricRegistry.name(DefaultNamespaceService.class,
        "saveAppTimer"));
    saveOwnerTimer = metrics.timer(MetricRegistry.name(DefaultNamespaceService.class,
        "saveOwnerTimer"));
    saveServiceTimer = metrics.timer(MetricRegistry.name(DefaultNamespaceService.class,
        "saveServiceTimer"));
    saveMemberTimer = metrics.timer(MetricRegistry.name(DefaultNamespaceService.class,
        "saveMemberTimer"));
    readNamespaceTimer = metrics.timer(MetricRegistry.name(DefaultNamespaceService.class,
        "readNamespaceTimer"));
    removeRelationTimer = metrics.timer(MetricRegistry.name(DefaultNamespaceService.class,
        "removeRelationTimer"));
  }
}
