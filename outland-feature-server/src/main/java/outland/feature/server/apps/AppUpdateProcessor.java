package outland.feature.server.apps;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import outland.feature.proto.AccessCollection;
import outland.feature.proto.App;
import outland.feature.proto.MemberAccess;
import outland.feature.proto.Owner;
import outland.feature.proto.OwnerCollection;
import outland.feature.proto.ServiceAccess;
import outland.feature.server.features.Ulid;

class AppUpdateProcessor {

  List<ServiceAccess> mergeServices(App app, ServiceAccess incoming) {
    final AccessCollection existingGrants = app.getGranted();
    final List<ServiceAccess> existingGrantsList = existingGrants.getServicesList();
    final List<ServiceAccess> updateGrantList = Lists.newArrayList();
    boolean found = false;
    for (ServiceAccess existing : existingGrantsList) {
      if (isMatchingServiceGrant(incoming, existing)) {
        updateGrantList.add(mergeServiceGrants(existing, incoming));
        found = true;
      } else {
        updateGrantList.add(existing);
      }
    }

    if (!found) {
      updateGrantList.add(prepareService(incoming));
    }
    return updateGrantList;
  }

  List<MemberAccess> mergeMembers(App app, MemberAccess incoming) {
    final AccessCollection existingGrants = app.getGranted();
    final List<MemberAccess> existingGrantsList = existingGrants.getMembersList();
    final List<MemberAccess> updateGrantList = Lists.newArrayList();
    boolean found = false;
    for (MemberAccess existing : existingGrantsList) {
      if (isMatchingMemberGrant(incoming, existing)) {
        updateGrantList.add(mergeMemberGrants(existing, incoming));
        found = true;
      } else {
        updateGrantList.add(existing);
      }
    }

    if (!found) {
      updateGrantList.add(prepareMember(incoming));
    }
    return updateGrantList;
  }

  List<Owner> mergeOwners(App app, Owner incoming) {
    final OwnerCollection existingOwners = app.getOwners();
    final List<Owner> existingOwnerList = existingOwners.getItemsList();
    final List<Owner> updateOwnerList = Lists.newArrayList();
    boolean found = false;
    for (Owner existing : existingOwnerList) {
      if (isMatchingOwner(incoming, existing)) {
        updateOwnerList.add(mergeOwners(existing, incoming));
        found = true;
      } else {
        updateOwnerList.add(existing);
      }
    }

    if (!found) {
      updateOwnerList.add(prepareOwner(incoming));
    }
    return updateOwnerList;
  }

  private boolean isMatchingServiceGrant(ServiceAccess incoming, ServiceAccess existing) {
    return existing.getKey().equals(incoming.getKey());
  }

  private boolean isMatchingOwner(Owner incoming, Owner existing) {
    return (!Strings.isNullOrEmpty(existing.getUsername()) && existing.getUsername()
        .equals(incoming.getUsername()))
        ||
        (!Strings.isNullOrEmpty(existing.getEmail()) && existing.getEmail()
            .equals(incoming.getEmail()));
  }

  private boolean isMatchingMemberGrant(MemberAccess incoming, MemberAccess existing) {
    return (!Strings.isNullOrEmpty(existing.getUsername()) && existing.getUsername()
        .equals(incoming.getUsername()))
        ||
        (!Strings.isNullOrEmpty(existing.getEmail()) && existing.getEmail()
            .equals(incoming.getEmail()));
  }

  MemberAccess prepareMember(MemberAccess member) {
    return member.toBuilder().setType("member").setId(mintMemberId()).build();
  }

  Owner prepareOwner(Owner owner) {
    return owner.toBuilder().setType("owner").setId(mintOwnerId()).build();
  }

  ServiceAccess prepareService(ServiceAccess service) {
    return service.toBuilder().setType("service").setId(mintServiceId()).build();
  }

  private Owner mergeOwners(Owner existing, Owner incoming) {
    return existing.toBuilder().mergeFrom(incoming).setId(existing.getId()).build();
  }

  private ServiceAccess mergeServiceGrants(ServiceAccess existing, ServiceAccess incoming) {
    return existing.toBuilder().mergeFrom(incoming).setId(existing.getId()).build();
  }

  private MemberAccess mergeMemberGrants(MemberAccess existing, MemberAccess incoming) {
    return existing.toBuilder().mergeFrom(incoming).setId(existing.getId()).build();
  }

  private String mintMemberId() {
    return "mbr_" + Ulid.random();
  }

  private String mintOwnerId() {
    return "usr_" + Ulid.random();
  }

  private String mintServiceId() {
    return "svc_" + Ulid.random();
  }
}
