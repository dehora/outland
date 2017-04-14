package outland.feature.server.groups;

import com.codahale.metrics.Meter;
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
import outland.feature.proto.Group;
import outland.feature.proto.MemberAccess;
import outland.feature.proto.Owner;
import outland.feature.proto.OwnerCollection;
import outland.feature.proto.ServiceAccess;
import outland.feature.server.Names;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;
import outland.feature.server.features.MetricsTimer;
import outland.feature.server.features.VersionService;

import static outland.feature.server.StructLog.kvp;

public class DefaultGroupService implements GroupService, MetricsTimer {

  public static final String SUBJECT_TYPE = "group";
  public static final String REL_MARK = "rel.";
  public static final String INV_MARK = "inv.";
  private static final Logger logger = LoggerFactory.getLogger(DefaultGroupService.class);
  private final GroupCache groupCache;
  private final GroupStorage groupStorage;
  private final VersionService versionService;
  private final GroupUpdateProcessor groupUpdateProcessor;

  private Timer saveNamespaceTimer;
  private Timer saveServiceTimer;
  private Timer saveOwnerTimer;
  private Timer saveMemberTimer;
  private Timer readNamespaceTimer;
  private Timer readCacheTimer;
  private Timer removeRelationTimer;
  private Meter loadCacheHitMeter;
  private Meter loadCacheMissMeter;
  private Timer addToCacheTimer;

  @Inject
  public DefaultGroupService(
      GroupCache groupCache,
      GroupStorage groupStorage,
      VersionService versionService,
      MetricRegistry metrics
  ) {
    this.groupCache = groupCache;
    this.groupStorage = groupStorage;
    this.versionService = versionService;
    this.groupUpdateProcessor = new GroupUpdateProcessor();
    configureMetrics(metrics);
  }

  @Override public Optional<Group> register(Group group) {
    logger.info("{} /group[{}]", kvp("op", "register"), TextFormat.shortDebugString(group));
    new GroupValidator().validateRegistrationThrowing(group);
    final Optional<Group> registration = processRegistration(group);

    registration.ifPresent(this::addToCache);
    return registration;
  }

  @Override public Group update(Group group) {
    logger.info("{} /group[{}]", kvp("op", "update"), TextFormat.shortDebugString(group));

    final Group update = processUpdate(group, builder -> {
    });

    addToCache(update);
    return update;
  }

  @Override public Group add(Group group, ServiceAccess service) {
    logger.info("{} /group[{}]/svc[{}]", kvp("op", "add.service"),
        TextFormat.shortDebugString(group), TextFormat.shortDebugString(service));

    final Group update = processUpdate(group,
        builder -> {
          AccessCollection.Builder accessBuilder = newGrantCollectionBuilder();
          accessBuilder.addAllServices(groupUpdateProcessor.mergeServices(group, service));
          accessBuilder.addAllMembers(group.getGranted().getMembersList());
          builder.setGranted(accessBuilder.buildPartial());
        });
    addToCache(update);
    return update;
  }

  @Override public Group add(Group group, MemberAccess member) {
    logger.info("{} /group[{}]/mbr[{}]", kvp("op", "add.member"),
        TextFormat.shortDebugString(group), TextFormat.shortDebugString(member));

    final Group update = processUpdate(group,
        builder -> {
          AccessCollection.Builder accessBuilder = newGrantCollectionBuilder();
          accessBuilder.addAllMembers(groupUpdateProcessor.mergeMembers(group, member));
          accessBuilder.addAllServices(group.getGranted().getServicesList());
          builder.setGranted(accessBuilder.buildPartial());
        });
    addToCache(update);
    return update;
  }

  @Override public Group add(Group group, final Owner incoming) {
    logger.info("{} /group[{}]/own[{}]", kvp("op", "add.owner"),
        TextFormat.shortDebugString(group), TextFormat.shortDebugString(incoming));

    final Group update = processUpdate(group,
        builder -> builder.setOwners(OwnerCollection.newBuilder()
            .addAllItems(groupUpdateProcessor.mergeOwners(group, incoming))));
    addToCache(update);
    return update;
  }

  @Override public Group removeServiceAccess(Group group, String serviceKey) {

    if (serviceKey == null) {
      return group;
    }

    final List<ServiceAccess> servicesList = group.getGranted().getServicesList();
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

    if (service == null) {
      return group;
    }

    final Group.Builder builder = group.toBuilder();
    builder.clearGranted();

    AccessCollection.Builder newBuilder = newGrantCollectionBuilder();
    newBuilder.addAllServices(wrapped);
    newBuilder.addAllMembers(group.getGranted().getMembersList());

    builder.setGranted(newBuilder.buildPartial());

    OffsetDateTime now = OffsetDateTime.now();
    String updateTime = GroupService.asString(now);
    builder.setUpdated(updateTime);

    final Group updated = builder.build();
    updateNamespaceInner(updated);
    removeServiceFromGraph(updated, service);
    addToCache(updated);
    return updated;
  }

  @Override public Group removeMemberAccess(Group group, String memberKey) {
    if (memberKey == null) {
      return group;
    }

    final List<MemberAccess> ownersList = group.getGranted().getMembersList();
    final ArrayList<MemberAccess> wrapped = Lists.newArrayList(ownersList);
    final Iterator<MemberAccess> iterator = wrapped.iterator();
    MemberAccess MemberAccess = null;

    while (iterator.hasNext()) {
      MemberAccess next = iterator.next();
      if (memberKey.equals(next.getUsername())) {
        MemberAccess = next;
        iterator.remove();
        break;
      }

      if (memberKey.equals(next.getEmail())) {
        MemberAccess = next;
        iterator.remove();
        break;
      }
    }

    if (MemberAccess == null) {
      return group;
    }

    final Group.Builder builder = group.toBuilder();
    builder.clearGranted();
    AccessCollection.Builder newBuilder = newGrantCollectionBuilder();
    newBuilder.addAllMembers(wrapped);
    newBuilder.addAllServices(group.getGranted().getServicesList());

    builder.setGranted(newBuilder.buildPartial());

    OffsetDateTime now = OffsetDateTime.now();
    String updateTime = GroupService.asString(now);
    builder.setUpdated(updateTime);

    final Group updated = builder.build();
    updateNamespaceInner(updated);
    removeMemberFromGraph(updated, MemberAccess);
    addToCache(updated);
    return updated;
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Override public Group removeOwner(Group group, String ownerKey) {

    if (ownerKey == null) {
      return group;
    }

    if (group.getOwners().getItemsCount() == 1) {
      throw new ServiceException(Problem.clientProblem("at_least_one_owner",
          "only one owner remaining, refusing to remove", 422));
    }

    final List<Owner> ownersList = group.getOwners().getItemsList();
    final ArrayList<Owner> wrapped = Lists.newArrayList(ownersList);
    final Iterator<Owner> iterator = wrapped.iterator();
    Owner owner = null;

    while (iterator.hasNext()) {
      Owner next = iterator.next();
      if (ownerKey.equals(next.getUsername())) {
        owner = next;
        iterator.remove();
        break;
      }

      if (ownerKey.equals(next.getEmail())) {
        owner = next;
        iterator.remove();
        break;
      }
    }

    if (owner == null) {
      return group;
    }

    final Group.Builder builder = group.toBuilder();
    builder.clearOwners();
    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType(Names.ownerCollectionType())
        .addAllItems(wrapped);
    builder.setOwners(oc);

    OffsetDateTime now = OffsetDateTime.now();
    String updateTime = GroupService.asString(now);
    builder.setUpdated(updateTime);

    final Group updated = builder.build();
    updateNamespaceInner(updated);
    removeOwnerFromGraph(updated, owner);
    addToCache(updated);
    return updated;
  }

  @Override public boolean hasOwner(String group, String usernameOrEmail) {
    return hasOwnerRelation(group, GroupService.OWNER, usernameOrEmail);
  }

  @Override public boolean hasServiceAccess(String group, String serviceKey) {
    return hasGrantRelation(group, GroupService.SERVICE, serviceKey);
  }

  @Override public boolean hasMemberAccess(String group, String usernameOrEmail) {
    return hasGrantRelation(group, GroupService.MEMBER, usernameOrEmail);
  }

  @Override public Optional<Group> loadByKey(String group) {

    final Optional<Group> cached = timed(readCacheTimer,
        () -> groupCache.findInCache(groupCache.buildCacheKey(group)));

    if(cached.isPresent()) {
      loadCacheHitMeter.mark();
      return cached;
    }

    loadCacheMissMeter.mark();

    return timed(readNamespaceTimer, () -> {
      final Optional<Group> maybe = groupStorage.loadByKey(group);
      maybe.ifPresent(this::addToCache);
      return maybe;
    });
  }

  private Owner prepareOwner(Owner owner) {
    return groupUpdateProcessor.prepareOwner(owner);
  }

  private ServiceAccess prepareService(ServiceAccess service) {
    return groupUpdateProcessor.prepareService(service);
  }

  private MemberAccess prepareMember(MemberAccess member) {
    return groupUpdateProcessor.prepareMember(member);
  }

  private Group processUpdate(Group group, Consumer<Group.Builder> updateExtra) {
    final Group.Builder builder = newNamespaceBuilder(group);
    builder.setUpdated(GroupService.asString(OffsetDateTime.now()));
    updateExtra.accept(builder);
    final Group updated = builder.build();
    updateNamespaceInner(updated);
    updateOwners(updated);
    updateMembers(updated);
    updateServices(updated);
    return updated;
  }

  private Optional<Group> processRegistration(Group group) {
    OffsetDateTime now = OffsetDateTime.now();
    String created = GroupService.asString(now);
    final Group.Builder builder = newNamespaceBuilder(group);
    builder.setId(Names.group(now));
    builder.setCreated(created);
    builder.setUpdated(created);

    List<Owner> ownersReady = Lists.newArrayList();
    group.getOwners().getItemsList().forEach(owner -> ownersReady.add(prepareOwner(owner)));
    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType(Names.ownerCollectionType())
        .addAllItems(ownersReady);
    builder.setOwners(oc);

    AccessCollection.Builder accessCollectionBuilder = newGrantCollectionBuilder();

    List<ServiceAccess> servicesReady = Lists.newArrayList();
    group.getGranted()
        .getServicesList()
        .forEach(service -> servicesReady.add(prepareService(service)));
    accessCollectionBuilder.addAllServices(servicesReady);

    List<MemberAccess> memberReady = Lists.newArrayList();
    group.getGranted().getMembersList().forEach(access -> memberReady.add(prepareMember(access)));
    accessCollectionBuilder.addAllMembers(memberReady);

    builder.clearGranted();
    builder.setGranted(accessCollectionBuilder.buildPartial());

    final Group registered = builder.build();

    // todo: the usual compensating write failure stuff
    registerNamespaceInner(registered);
    registerOwners(registered);
    registerServices(registered);
    registerMembers(registered);

    return Optional.of(registered);
  }

  private AccessCollection.Builder newGrantCollectionBuilder() {
    return AccessCollection.newBuilder().setType(Names.accessCollectionType());
  }

  private Group.Builder newNamespaceBuilder(Group group) {
    return group.toBuilder().setType(DefaultGroupService.SUBJECT_TYPE);
  }

  private boolean hasOwnerRelation(String group, String relatedType, String relatedKey) {
    return hasRelation(group, relatedType, relatedKey, OWNER_RELATION);
  }

  private boolean hasGrantRelation(String group, String relatedType, String relatedKey) {
    return hasRelation(group, relatedType, relatedKey, ACCESS_RELATION);
  }

  private boolean hasRelation(String group, String relatedType, String relatedKey,
      String relation) {
    final String subjectType = DefaultGroupService.SUBJECT_TYPE;
    final String subjectKey = group;
    final String objectType = relatedType;
    final String objectKey = relatedKey;
    final String inv = INV_MARK;

    final String inverseRelationHashKey = objectType + "." + objectKey;
    final String inverseRelationRangeKey = inv + relation + "." + subjectType + "." + subjectKey;

    return groupStorage.queryRelationExists(inverseRelationHashKey, inverseRelationRangeKey);
  }

  private void registerNamespaceInner(Group registered) {
    timed(saveNamespaceTimer, () -> groupStorage.create(registered));
  }

  private void registerServices(Group group) {
    group.getGranted().getServicesList().forEach(service -> addServiceToGraph(group, service));
  }

  private void registerMembers(Group group) {
    group.getGranted().getMembersList().forEach(member -> addMemberToGraph(group, member));
  }

  private void registerOwners(Group group) {
    group.getOwners().getItemsList().forEach(service -> addOwnerToGraph(group, service));
  }

  private void updateNamespaceInner(Group registered) {
    timed(saveNamespaceTimer, () -> groupStorage.save(registered));
  }

  private void updateServices(Group group) {
    group.getGranted().getServicesList().forEach(service -> addServiceToGraph(group, service));
  }

  private void updateMembers(Group group) {
    group.getGranted().getMembersList().forEach(member -> addMemberToGraph(group, member));
  }

  private void updateOwners(Group group) {
    group.getOwners().getItemsList().forEach(service -> addOwnerToGraph(group, service));
  }

  private void addServiceToGraph(Group group, ServiceAccess service) {

    final String relation = ACCESS_RELATION;
    final String subjectType = DefaultGroupService.SUBJECT_TYPE;
    final String subjectKey = group.getKey();
    final String objectType = GroupService.SERVICE;
    final String objectKey = service.getKey();

    saveGraphRelation(
        group, relation, subjectType, subjectKey, objectType, objectKey, saveServiceTimer);
  }

  private void addMemberToGraph(Group group, MemberAccess member) {
    final String relation = ACCESS_RELATION;
    final String subjectType = DefaultGroupService.SUBJECT_TYPE;
    final String subjectKey = group.getKey();
    final String objectType = GroupService.MEMBER;

    if (!Strings.isNullOrEmpty(member.getUsername())) {
      final String objectKey = member.getUsername();
      saveGraphRelation(
          group, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }

    if (!Strings.isNullOrEmpty(member.getEmail())) {
      final String objectKey = member.getEmail();
      saveGraphRelation(
          group, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }
  }

  private void addOwnerToGraph(Group group, Owner owner) {
    final String relation = OWNER_RELATION;
    final String subjectType = DefaultGroupService.SUBJECT_TYPE;
    final String subjectKey = group.getKey();
    final String objectType = GroupService.OWNER;

    if (!Strings.isNullOrEmpty(owner.getUsername())) {
      final String objectKey = owner.getUsername();
      saveGraphRelation(
          group, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }

    if (!Strings.isNullOrEmpty(owner.getEmail())) {
      final String objectKey = owner.getEmail();
      saveGraphRelation(
          group, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }
  }

  private void saveGraphRelation(
      Group group,
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
      group distinctly from the service members of an group, or just grab both membership
      kinds.

      +-----------------------------+------------------------------------------+
      | hash                        |   range                                  |
      +-----------------------------+------------------------------------------+
      | $subject_type.$subject_key  |   rel.$relation.$object_type.$object_key |
      +-----------------------------+------------------------------------------+
      | $object_type.$object_key    |   inv.$relation.$object_type.$object_key |
      +-----------------------------+------------------------------------------+
     */

    final String rel = REL_MARK;
    final String inv = INV_MARK;

    final String relationHashKey = subjectType + "." + subjectKey;
    final String relationRangeKey = rel + relation + "." + objectType + "." + objectKey;
    timed(timer,
        () -> groupStorage.saveRelation(group, relationHashKey, relationRangeKey));

    final String inverseRelationHashKey = objectType + "." + objectKey;
    final String inverseRelationRangeKey = inv + relation + "." + subjectType + "." + subjectKey;
    timed(timer,
        () -> groupStorage.saveRelation(group, inverseRelationHashKey, inverseRelationRangeKey));
  }

  private void removeOwnerFromGraph(Group group, Owner owner) {
    final String relation = OWNER_RELATION;
    final String subjectType = SUBJECT_TYPE;
    final String subjectKey = group.getKey();
    final String objectType = GroupService.OWNER;

    if (!Strings.isNullOrEmpty(owner.getUsername())) {
      final String objectKey = owner.getUsername();
      removeGraphRelation(
          group, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }

    if (!Strings.isNullOrEmpty(owner.getEmail())) {
      final String objectKey = owner.getEmail();
      removeGraphRelation(
          group, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }
  }

  private void removeMemberFromGraph(Group group, MemberAccess member) {
    final String relation = ACCESS_RELATION;
    final String subjectType = SUBJECT_TYPE;
    final String subjectKey = group.getKey();
    final String objectType = GroupService.MEMBER;

    if (!Strings.isNullOrEmpty(member.getUsername())) {
      final String objectKey = member.getUsername();
      removeGraphRelation(
          group, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }

    if (!Strings.isNullOrEmpty(member.getEmail())) {
      final String objectKey = member.getEmail();
      removeGraphRelation(
          group, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }
  }

  private void removeServiceFromGraph(Group group, ServiceAccess service) {
    final String relation = ACCESS_RELATION;
    final String subjectType = SUBJECT_TYPE;
    final String subjectKey = group.getKey();
    final String objectType = GroupService.SERVICE;
    final String objectKey = service.getKey();

    removeGraphRelation(
        group, relation, subjectType, subjectKey, objectType, objectKey, removeRelationTimer);
  }

  private void removeGraphRelation(
      Group group,
      String relation,
      String subjectType,
      String subjectKey,
      String objectType,
      String objectKey,
      Timer timer
  ) {
    final String rel = REL_MARK;
    final String inv = INV_MARK;

    final String relationHashKey = subjectType + "." + subjectKey;
    final String relationRangeKey = rel + relation + "." + objectType + "." + objectKey;
    timed(timer,
        () -> groupStorage.removeRelation(group, relationHashKey, relationRangeKey));

    final String inverseRelationHashKey = objectType + "." + objectKey;
    final String inverseRelationRangeKey = inv + relation + "." + subjectType + "." + subjectKey;
    timed(timer,
        () -> groupStorage.removeRelation(group, inverseRelationHashKey, inverseRelationRangeKey));
  }

  private void addToCache(Group group) {
    timed(addToCacheTimer, () -> groupCache.addToCache(group));
  }

  private void configureMetrics(MetricRegistry metrics) {
    saveNamespaceTimer = metrics.timer(MetricRegistry.name(DefaultGroupService.class,
        "saveNamespaceTimer"));
    saveOwnerTimer = metrics.timer(MetricRegistry.name(DefaultGroupService.class,
        "saveOwnerTimer"));
    saveServiceTimer = metrics.timer(MetricRegistry.name(DefaultGroupService.class,
        "saveServiceTimer"));
    saveMemberTimer = metrics.timer(MetricRegistry.name(DefaultGroupService.class,
        "saveMemberTimer"));
    readNamespaceTimer = metrics.timer(MetricRegistry.name(DefaultGroupService.class,
        "readNamespaceTimer"));
    removeRelationTimer = metrics.timer(MetricRegistry.name(DefaultGroupService.class,
        "removeRelationTimer"));
    readCacheTimer = metrics.timer(MetricRegistry.name(DefaultGroupService.class,
        "readCacheTimer"));
    addToCacheTimer = metrics.timer(MetricRegistry.name(DefaultGroupService.class,
        "addToCacheTimer"));
    loadCacheMissMeter = metrics.meter(MetricRegistry.name(DefaultGroupService.class,
        "loadCacheMissMeter"));
    loadCacheHitMeter = metrics.meter(MetricRegistry.name(DefaultGroupService.class,
        "loadCacheHitMeter"));
  }
}
