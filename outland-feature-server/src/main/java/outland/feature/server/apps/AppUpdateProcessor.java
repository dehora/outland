package outland.feature.server.apps;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import outland.feature.proto.App;
import outland.feature.proto.GrantCollection;
import outland.feature.proto.MemberGrant;
import outland.feature.proto.Owner;
import outland.feature.proto.OwnerCollection;
import outland.feature.proto.ServiceGrant;
import outland.feature.server.features.Ulid;

class AppUpdateProcessor {

  List<ServiceGrant> mergeServices(App app, ServiceGrant incoming) {
    final GrantCollection existingGrants = app.getGranted();
    final List<ServiceGrant> existingGrantsList = existingGrants.getServicesList();
    final List<ServiceGrant> updateGrantList = Lists.newArrayList();
    boolean found = false;
    for (ServiceGrant existing : existingGrantsList) {
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

  List<MemberGrant> mergeMembers(App app, MemberGrant incoming) {
    final GrantCollection existingGrants = app.getGranted();
    final List<MemberGrant> existingGrantsList = existingGrants.getMembersList();
    final List<MemberGrant> updateGrantList = Lists.newArrayList();
    boolean found = false;
    for (MemberGrant existing : existingGrantsList) {
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

  private boolean isMatchingServiceGrant(ServiceGrant incoming, ServiceGrant existing) {
    return existing.getKey().equals(incoming.getKey());
  }

  private boolean isMatchingOwner(Owner incoming, Owner existing) {
    return (!Strings.isNullOrEmpty(existing.getUsername()) && existing.getUsername()
        .equals(incoming.getUsername()))
        ||
        (!Strings.isNullOrEmpty(existing.getEmail()) && existing.getEmail()
            .equals(incoming.getEmail()));
  }

  private boolean isMatchingMemberGrant(MemberGrant incoming, MemberGrant existing) {
    return (!Strings.isNullOrEmpty(existing.getUsername()) && existing.getUsername()
        .equals(incoming.getUsername()))
        ||
        (!Strings.isNullOrEmpty(existing.getEmail()) && existing.getEmail()
            .equals(incoming.getEmail()));
  }

  MemberGrant prepareMember(MemberGrant member) {
    return member.toBuilder().setType("member").setId(mintMemberId()).build();
  }

  Owner prepareOwner(Owner owner) {
    return owner.toBuilder().setType("owner").setId(mintOwnerId()).build();
  }

  ServiceGrant prepareService(ServiceGrant service) {
    return service.toBuilder().setType("service").setId(mintServiceId()).build();
  }

  private Owner mergeOwners(Owner existing, Owner incoming) {
    return existing.toBuilder().mergeFrom(incoming).setId(existing.getId()).build();
  }

  private ServiceGrant mergeServiceGrants(ServiceGrant existing, ServiceGrant incoming) {
    return existing.toBuilder().mergeFrom(incoming).setId(existing.getId()).build();
  }

  private MemberGrant mergeMemberGrants(MemberGrant existing, MemberGrant incoming) {
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
