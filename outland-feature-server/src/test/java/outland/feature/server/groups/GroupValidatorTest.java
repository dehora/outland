package outland.feature.server.groups;

import org.junit.Test;
import outland.feature.proto.Group;
import outland.feature.proto.Owner;
import outland.feature.proto.OwnerCollection;
import outland.feature.server.ServiceException;

import static org.junit.Assert.*;

public class GroupValidatorTest {

  @Test
  public void validateRegistrationThrowingOk() {
    Group.Builder builder = Group.newBuilder();

    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType("owner.collection")
        .addItems(Owner.newBuilder().setName("Bob").setUsername("bob"));

    builder.setOwners(oc)
        .setKey("key1")
        .setName("ns 1");

    try {
      new GroupValidator().validateRegistrationThrowing(builder.build());
    } catch (ServiceException e) {
      fail();
    }
  }

  /*
  these assume the order of validator checks so not that great. a better validator would
  roll up all errors and throw just once
   */

  @Test
  public void validateRegistrationThrowingNoOwner() {
    Group.Builder builder = Group.newBuilder();
    callValidate(builder, "no_owner_for_group", 422);
  }

  @Test
  public void validateRegistrationThrowingJunkOwner() {
    Group.Builder builder = Group.newBuilder();
    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType("owner.collection")
        .addItems(Owner.newBuilder().setName("Bob"));
    builder.setOwners(oc);

    callValidate(builder, "incomplete_owner_for_group", 422);
  }

  @Test
  public void validateRegistrationThrowingNoKey() {
    Group.Builder builder = Group.newBuilder();
    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType("owner.collection")
        .addItems(Owner.newBuilder().setName("Bob").setUsername("bob"));
    builder.setOwners(oc);

    callValidate(builder, "missing_key_for_group", 422);
  }

  @Test
  public void validateRegistrationThrowingNoName() {
    Group.Builder builder = Group.newBuilder();
    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType("owner.collection")
        .addItems(Owner.newBuilder().setName("Bob").setUsername("bob"));
    builder.setOwners(oc)
        .setKey("key1");

    callValidate(builder, "missing_name_for_group", 422);
  }

  private void callValidate(Group.Builder builder, String expectedTitle, int expectedCode) {
    try {
      new GroupValidator().validateRegistrationThrowing(builder.build());
      fail();
    } catch (ServiceException e) {
      assertEquals(expectedCode, e.problem().status());
      assertSame(expectedTitle, e.problem().title());
    }
  }

}