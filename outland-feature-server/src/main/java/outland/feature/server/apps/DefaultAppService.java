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
import outland.feature.proto.App;
import outland.feature.proto.GrantCollection;
import outland.feature.proto.MemberGrant;
import outland.feature.proto.Owner;
import outland.feature.proto.ServiceGrant;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;
import outland.feature.server.features.MetricsTimer;
import outland.feature.server.features.Ulid;
import outland.feature.server.features.VersionService;

import static outland.feature.server.StructLog.kvp;

public class DefaultAppService implements AppService, MetricsTimer {

  private static final Logger logger = LoggerFactory.getLogger(DefaultAppService.class);

  private final AppStorage appStorage;
  private final VersionService versionService;

  private Timer saveAppTimer;
  private Timer saveServiceTimer;
  private Timer saveOwnerTimer;
  private Timer saveMemberTimer;
  private Timer readAppTimer;
  private Timer removeRelationTimer;

  @Inject
  public DefaultAppService(
      AppStorage appStorage,
      VersionService versionService,
      MetricRegistry metrics
  ) {
    this.appStorage = appStorage;
    this.versionService = versionService;
    configureMetrics(metrics);
  }

  @Override public Optional<App> registerApp(App app) {
    logger.info("{} /app[{}]", kvp("op", "registerApp"), TextFormat.shortDebugString(app));
    new AppValidator().validateAppRegistrationThrowing(app);
    return processRegistration(app);
  }

  @Override public App updateApp(App app) {
    logger.info("{} /app[{}]", kvp("op", "updateApp"), TextFormat.shortDebugString(app));

    return processUpdate(app, builder -> {});
  }

  @Override public App addToApp(App app, ServiceGrant service) {
    logger.info("{} /app[{}]/svc[{}]", kvp("op", "addToApp.service"),
        TextFormat.shortDebugString(app), TextFormat.shortDebugString(service));

    return processUpdate(app,
        builder -> {
          final GrantCollection granted = app.getGranted();
          List<ServiceGrant> grants = granted.getServicesList();
          boolean found = false;
          for (ServiceGrant grant : grants) {
            if(grant.getKey().equals(service.getKey())) {
              found = true;
              break;
            }
          }

          if (!found) {
            grants = Lists.newArrayList(grants);
            grants.add(prepareService(service));
          }

          GrantCollection.Builder grantBuilder = newGrantCollectionBuilder();
          grantBuilder.addAllServices(grants);
          grantBuilder.addAllMembers(granted.getMembersList());
          builder.setGranted(grantBuilder.buildPartial());
        });
  }

  @Override public App addToApp(App app, MemberGrant member) {
    logger.info("{} /app[{}]/mbr[{}]", kvp("op", "addToApp.member"),
        TextFormat.shortDebugString(app), TextFormat.shortDebugString(member));

    return processUpdate(app,
        builder -> {
          final GrantCollection granted = app.getGranted();
          List<MemberGrant> grants = granted.getMembersList();
          boolean found = false;
          for (MemberGrant grant : grants) {
            final String username = grant.getUsername();
            if (!Strings.isNullOrEmpty(username) && username.equals(member.getUsername())) {
              found = true;
              break;
            }

            final String email = grant.getEmail();
            if (!Strings.isNullOrEmpty(email) && email.equals(member.getEmail())) {
              found = true;
              break;
            }
          }

          if (!found) {
            grants = Lists.newArrayList(grants);
            grants.add(prepareMember(member));
          }

          GrantCollection.Builder grantBuilder = newGrantCollectionBuilder();
          grantBuilder.addAllMembers(grants);
          grantBuilder.addAllServices(granted.getServicesList());
          builder.setGranted(grantBuilder.buildPartial());
        });
  }

  @Override public App addToApp(App app, final Owner owner) {
    logger.info("{} /app[{}]/own[{}]", kvp("op", "addToApp.owner"),
        TextFormat.shortDebugString(app), TextFormat.shortDebugString(owner));

    return processUpdate(app, builder -> builder.addOwners(prepareOwner(owner)));
  }

  @Override public App removeServiceGrant(App app, String serviceKey) {

    if(serviceKey == null) {
      return  app;
    }

    final List<ServiceGrant> servicesList = app.getGranted().getServicesList();
    final ArrayList<ServiceGrant> wrapped = Lists.newArrayList(servicesList);
    final Iterator<ServiceGrant> iterator = wrapped.iterator();
    ServiceGrant service = null;
    while (iterator.hasNext()) {
      final ServiceGrant next = iterator.next();
      if (serviceKey.equals(next.getKey())) {
        service = next;
        iterator.remove();
        break;
      }
    }

    if(service == null) {
      return app;
    }

    final App.Builder builder = app.toBuilder();
    builder.clearGranted();

    GrantCollection.Builder newBuilder = newGrantCollectionBuilder();
    newBuilder.addAllServices(wrapped);
    newBuilder.addAllMembers(app.getGranted().getMembersList());

    builder.setGranted(newBuilder.buildPartial());

    OffsetDateTime now = OffsetDateTime.now();
    String updateTime = AppService.asString(now);
    builder.setUpdated(updateTime);

    final App updated = builder.build();
    updateAppInner(updated);
    removeServiceFromGraph(updated, service);
    return updated;
  }

  @Override public App removeMemberGrant(App app, String memberKey) {
    if(memberKey == null) {
      return  app;
    }

    final List<MemberGrant> ownersList = app.getGranted().getMembersList();
    final ArrayList<MemberGrant> wrapped = Lists.newArrayList(ownersList);
    final Iterator<MemberGrant> iterator = wrapped.iterator();
    MemberGrant memberGrant = null;

    while (iterator.hasNext()) {
      MemberGrant next = iterator.next();
      if(memberKey.equals(next.getUsername())) {
        memberGrant  = next;
        iterator.remove();
        break;
      }

      if(memberKey.equals(next.getEmail())) {
        memberGrant  = next;
        iterator.remove();
        break;
      }
    }

    if(memberGrant == null) {
      return app;
    }

    final App.Builder builder = app.toBuilder();
    builder.clearGranted();
    GrantCollection.Builder newBuilder = newGrantCollectionBuilder();
    newBuilder.addAllMembers(wrapped);
    newBuilder.addAllServices(app.getGranted().getServicesList());

    builder.setGranted(newBuilder.buildPartial());

    OffsetDateTime now = OffsetDateTime.now();
    String updateTime = AppService.asString(now);
    builder.setUpdated(updateTime);

    final App updated = builder.build();
    updateAppInner(updated);
    removeMemberFromGraph(updated, memberGrant);
    return updated;

  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Override public App removeOwner(App app, String ownerKey) {

    if(ownerKey == null) {
      return app;
    }

    if(app.getOwnersCount() == 1) {
      throw new ServiceException(Problem.clientProblem("at_least_one_owner",
          "only one owner remaining, refusing to remove", 422));
    }

    final List<Owner> ownersList = app.getOwnersList();
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
      return app;
    }

    final App.Builder builder = app.toBuilder();
    builder.clearOwners();
    builder.addAllOwners(wrapped);

    OffsetDateTime now = OffsetDateTime.now();
    String updateTime = AppService.asString(now);
    builder.setUpdated(updateTime);

    final App updated = builder.build();
    updateAppInner(updated);
    removeOwnerFromGraph(updated, owner);
    return updated;
  }

  @Override public boolean appHasOwner(String appKey, String usernameOrEmail) {
    return appHasOwnerRelation(appKey, AppService.OWNER, usernameOrEmail);
  }

  @Override public boolean appHasServiceGrant(String appKey, String serviceKey) {
    return appHasGrantRelation(appKey, AppService.SERVICE, serviceKey);
  }

  @Override public boolean appHasMemberGrant(String appKey, String usernameOrEmail) {
    return appHasGrantRelation(appKey, AppService.MEMBER, usernameOrEmail);
  }

  @Override public Optional<App> loadAppByKey(String appKey) {
    return timed(readAppTimer, () -> appStorage.loadAppByKey(appKey));
  }

  private Owner prepareOwner(Owner owner) {
    return owner.toBuilder().setType("owner").setId(mintOwnerId()).build();
  }

  private ServiceGrant prepareService(ServiceGrant service) {
    return service.toBuilder().setType("service").setId(mintServiceId()).build();
  }

  private MemberGrant prepareMember(MemberGrant member) {
    return member.toBuilder().setType("member").setId(mintMemberId()).build();
  }

  private App processUpdate(App app, Consumer<App.Builder> updateExtra) {
    final App.Builder builder = newAppBuilder(app);
    builder.setUpdated(AppService.asString(OffsetDateTime.now()));
    updateExtra.accept(builder);
    final App updated = builder.build();
    updateAppInner(updated);
    updateOwners(updated);
    updateMembers(updated);
    updateServices(updated);
    return updated;
  }

  private Optional<App> processRegistration(App app) {
    OffsetDateTime now = OffsetDateTime.now();
    String created = AppService.asString(now);
    final App.Builder appBuilder = newAppBuilder(app);
    appBuilder.setId("app_" + Ulid.random(now.toInstant().toEpochMilli()));
    appBuilder.setCreated(created);
    appBuilder.setUpdated(created);

    List<Owner> ownersReady = Lists.newArrayList();
    app.getOwnersList().forEach(owner -> ownersReady.add(prepareOwner(owner)));
    appBuilder.clearOwners().addAllOwners(ownersReady);

    GrantCollection.Builder grantCollectionBuilder = newGrantCollectionBuilder();

    List<ServiceGrant> servicesReady = Lists.newArrayList();
    app.getGranted().getServicesList().forEach(service -> servicesReady.add(prepareService(service)));
    grantCollectionBuilder.addAllServices(servicesReady);

    List<MemberGrant> memberReady = Lists.newArrayList();
    app.getGranted().getMembersList().forEach(grant -> memberReady.add(prepareMember(grant)));
    grantCollectionBuilder.addAllMembers(memberReady);

    appBuilder.clearGranted();
    appBuilder.setGranted(grantCollectionBuilder.buildPartial());

    final App registered = appBuilder.build();

    // todo: the usual compensating write failure stuff
    registerAppInner(registered);
    registerOwners(registered);
    registerServices(registered);
    registerMembers(registered);

    return Optional.of(registered);
  }

  private GrantCollection.Builder newGrantCollectionBuilder() {
    return GrantCollection.newBuilder().setType("grant.collection");
  }

  private App.Builder newAppBuilder(App app) {
    return app.toBuilder().setType("app");
  }

  private boolean appHasOwnerRelation(String appKey, String relatedType, String relatedKey) {
    return appHasRelation(appKey, relatedType, relatedKey, OWNER_RELATION);
  }

  private boolean appHasGrantRelation(String appKey, String relatedType, String relatedKey) {
    return appHasRelation(appKey, relatedType, relatedKey, GRANT_RELATION);
  }

  private boolean appHasRelation(String appKey, String relatedType, String relatedKey,
      String relation) {
    final String subjectType = "app";
    final String subjectKey = appKey;
    final String objectType = relatedType;
    final String objectKey = relatedKey;
    final String inv = "inv.";

    final String inverseRelationHashKey = objectType + "." + objectKey;
    final String inverseRelationRangeKey = inv + relation + "." + subjectType + "." + subjectKey;

    return appStorage.queryRelationExists(inverseRelationHashKey, inverseRelationRangeKey);
  }

  private void registerAppInner(App registered) {
    timed(saveAppTimer, () -> appStorage.saveApp(registered));
  }

  private void registerServices(App app) {
    app.getGranted().getServicesList().forEach(service -> addServiceToGraph(app, service));
  }

  private void registerMembers(App app) {
    app.getGranted().getMembersList().forEach(member -> addMemberToGraph(app, member));
  }

  private void registerOwners(App app) {
    app.getOwnersList().forEach(service -> addOwnerToGraph(app, service));
  }

  private String mintOwnerId() {
    return "usr_" + Ulid.random();
  }

  private String mintServiceId() {
    return "svc_" + Ulid.random();
  }

  private String mintMemberId() {
    return "mbr_" + Ulid.random();
  }

  private void updateAppInner(App registered) {
    timed(saveAppTimer, () -> appStorage.saveApp(registered));
  }

  private void updateServices(App app) {
    app.getGranted().getServicesList().forEach(service -> addServiceToGraph(app, service));
  }

  private void updateMembers(App app) {
    app.getGranted().getMembersList().forEach(member -> addMemberToGraph(app, member));
  }

  private void updateOwners(App app) {
    app.getOwnersList().forEach(service -> addOwnerToGraph(app, service));
  }

  private void addServiceToGraph(App app, ServiceGrant service) {

    final String relation = GRANT_RELATION;
    final String subjectType = "app";
    final String subjectKey = app.getKey();
    final String objectType = AppService.SERVICE;
    final String objectKey = service.getKey();

    saveGraphRelation(
        app, relation, subjectType, subjectKey, objectType, objectKey, saveServiceTimer);
  }

  private void addMemberToGraph(App app, MemberGrant member) {
    final String relation = GRANT_RELATION;
    final String subjectType = "app";
    final String subjectKey = app.getKey();
    final String objectType = AppService.MEMBER;

    if(! Strings.isNullOrEmpty(member.getUsername())) {
      final String objectKey = member.getUsername();
      saveGraphRelation(
          app, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }

    if(! Strings.isNullOrEmpty(member.getEmail())) {
      final String objectKey = member.getEmail();
      saveGraphRelation(
          app, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }
  }

  private void addOwnerToGraph(App app, Owner owner) {
    final String relation = OWNER_RELATION;
    final String subjectType = "app";
    final String subjectKey = app.getKey();
    final String objectType = AppService.OWNER;

    if(! Strings.isNullOrEmpty(owner.getUsername())) {
      final String objectKey = owner.getUsername();
      saveGraphRelation(
          app, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }

    if(! Strings.isNullOrEmpty(owner.getEmail())) {
      final String objectKey = owner.getEmail();
      saveGraphRelation(
          app, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }

  }

  private void saveGraphRelation(
      App app,
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
      app distinctly from the service members of an app, or just grab both membership
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
        () -> appStorage.saveRelation(app, relationHashKey, relationRangeKey));

    final String inverseRelationHashKey = objectType + "." + objectKey;
    final String inverseRelationRangeKey = inv + relation + "." + subjectType + "." + subjectKey;
    timed(timer,
        () -> appStorage.saveRelation(app, inverseRelationHashKey, inverseRelationRangeKey));
  }

  private void removeOwnerFromGraph(App app, Owner owner) {
    final String relation = OWNER_RELATION;
    final String subjectType = "app";
    final String subjectKey = app.getKey();
    final String objectType = AppService.OWNER;

    if(! Strings.isNullOrEmpty(owner.getUsername())) {
      final String objectKey = owner.getUsername();
      removeGraphRelation(
          app, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }

    if(! Strings.isNullOrEmpty(owner.getEmail())) {
      final String objectKey = owner.getEmail();
      removeGraphRelation(
          app, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }
  }

  private void removeMemberFromGraph(App app, MemberGrant member) {
    final String relation = GRANT_RELATION;
    final String subjectType = "app";
    final String subjectKey = app.getKey();
    final String objectType = AppService.MEMBER;

    if(! Strings.isNullOrEmpty(member.getUsername())) {
      final String objectKey = member.getUsername();
      removeGraphRelation(
          app, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }

    if(! Strings.isNullOrEmpty(member.getEmail())) {
      final String objectKey = member.getEmail();
      removeGraphRelation(
          app, relation, subjectType, subjectKey, objectType, objectKey, saveOwnerTimer);
    }
  }

  private void removeServiceFromGraph(App app, ServiceGrant service) {
    final String relation = GRANT_RELATION;
    final String subjectType = "app";
    final String subjectKey = app.getKey();
    final String objectType = AppService.SERVICE;
    final String objectKey = service.getKey();

    removeGraphRelation(
        app, relation, subjectType, subjectKey, objectType, objectKey, removeRelationTimer);
  }

  private void removeGraphRelation(
      App app,
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
        () -> appStorage.removeRelation(app, relationHashKey, relationRangeKey));

    final String inverseRelationHashKey = objectType + "." + objectKey;
    final String inverseRelationRangeKey = inv + relation + "." + subjectType + "." + subjectKey;
    timed(timer,
        () -> appStorage.removeRelation(app, inverseRelationHashKey, inverseRelationRangeKey));
  }

  private void configureMetrics(MetricRegistry metrics) {
    saveAppTimer = metrics.timer(MetricRegistry.name(DefaultAppService.class,
        "saveAppTimer"));
    saveOwnerTimer = metrics.timer(MetricRegistry.name(DefaultAppService.class,
        "saveOwnerTimer"));
    saveServiceTimer = metrics.timer(MetricRegistry.name(DefaultAppService.class,
        "saveServiceTimer"));
    saveMemberTimer = metrics.timer(MetricRegistry.name(DefaultAppService.class,
        "saveMemberTimer"));
    readAppTimer = metrics.timer(MetricRegistry.name(DefaultAppService.class,
        "readAppTimer"));
    removeRelationTimer = metrics.timer(MetricRegistry.name(DefaultAppService.class,
        "removeRelationTimer"));
  }
}
