package outland.feature.server.apps;

import org.junit.Test;
import outland.feature.proto.App;
import outland.feature.proto.Owner;
import outland.feature.proto.OwnerCollection;
import outland.feature.server.ServiceException;

import static org.junit.Assert.*;

public class AppValidatorTest {

  @Test
  public void validateAppRegistrationThrowingOk() {
    App.Builder builder = App.newBuilder();

    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType("owner.collection")
        .addItems(Owner.newBuilder().setName("Bob").setUsername("bob"));

    builder.setOwners(oc)
        .setKey("key1")
        .setName("app 1");

    try {
      new AppValidator().validateAppRegistrationThrowing(builder.build());
    } catch (ServiceException e) {
      fail();
    }
  }

  /*
  these assume the order of validator checks so not that great. a better validator would
  roll up all errors and throw just once
   */

  @Test
  public void validateAppRegistrationThrowingNoOwner() {
    App.Builder builder = App.newBuilder();
    callValidate(builder, "no_owner_for_app", 422);
  }

  @Test
  public void validateAppRegistrationThrowingJunkOwner() {
    App.Builder builder = App.newBuilder();
    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType("owner.collection")
        .addItems(Owner.newBuilder().setName("Bob"));
    builder.setOwners(oc);

    callValidate(builder, "incomplete_owner_for_app", 422);
  }

  @Test
  public void validateAppRegistrationThrowingNoKey() {
    App.Builder builder = App.newBuilder();
    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType("owner.collection")
        .addItems(Owner.newBuilder().setName("Bob").setUsername("bob"));
    builder.setOwners(oc);

    callValidate(builder, "missing_key_for_app", 422);
  }

  @Test
  public void validateAppRegistrationThrowingNoName() {
    App.Builder builder = App.newBuilder();
    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType("owner.collection")
        .addItems(Owner.newBuilder().setName("Bob").setUsername("bob"));
    builder.setOwners(oc)
        .setKey("key1");

    callValidate(builder, "missing_name_for_app", 422);
  }

  private void callValidate(App.Builder builder, String expectedTitle, int expectedCode) {
    try {
      new AppValidator().validateAppRegistrationThrowing(builder.build());
      fail();
    } catch (ServiceException e) {
      assertEquals(expectedCode, e.problem().status());
      assertSame(expectedTitle, e.problem().title());
    }
  }

}