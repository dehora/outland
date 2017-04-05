package outland.feature.server.namespaces;

import org.junit.Test;
import outland.feature.proto.Namespace;
import outland.feature.proto.Owner;
import outland.feature.proto.OwnerCollection;
import outland.feature.server.ServiceException;

import static org.junit.Assert.*;

public class NamespaceValidatorTest {

  @Test
  public void validateAppRegistrationThrowingOk() {
    Namespace.Builder builder = Namespace.newBuilder();

    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType("owner.collection")
        .addItems(Owner.newBuilder().setName("Bob").setUsername("bob"));

    builder.setOwners(oc)
        .setKey("key1")
        .setName("ns 1");

    try {
      new NamespaceValidator().validateNamespaceRegistrationThrowing(builder.build());
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
    Namespace.Builder builder = Namespace.newBuilder();
    callValidate(builder, "no_owner_for_namespace", 422);
  }

  @Test
  public void validateAppRegistrationThrowingJunkOwner() {
    Namespace.Builder builder = Namespace.newBuilder();
    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType("owner.collection")
        .addItems(Owner.newBuilder().setName("Bob"));
    builder.setOwners(oc);

    callValidate(builder, "incomplete_owner_for_namespace", 422);
  }

  @Test
  public void validateAppRegistrationThrowingNoKey() {
    Namespace.Builder builder = Namespace.newBuilder();
    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType("owner.collection")
        .addItems(Owner.newBuilder().setName("Bob").setUsername("bob"));
    builder.setOwners(oc);

    callValidate(builder, "missing_key_for_namespace", 422);
  }

  @Test
  public void validateAppRegistrationThrowingNoName() {
    Namespace.Builder builder = Namespace.newBuilder();
    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType("owner.collection")
        .addItems(Owner.newBuilder().setName("Bob").setUsername("bob"));
    builder.setOwners(oc)
        .setKey("key1");

    callValidate(builder, "missing_name_for_namespace", 422);
  }

  private void callValidate(Namespace.Builder builder, String expectedTitle, int expectedCode) {
    try {
      new NamespaceValidator().validateNamespaceRegistrationThrowing(builder.build());
      fail();
    } catch (ServiceException e) {
      assertEquals(expectedCode, e.problem().status());
      assertSame(expectedTitle, e.problem().title());
    }
  }

}