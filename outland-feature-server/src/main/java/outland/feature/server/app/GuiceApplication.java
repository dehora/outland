package outland.feature.server.app;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import com.squarespace.jersey2.guice.JerseyGuiceModule;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import io.dropwizard.Application;
import io.dropwizard.Bundle;
import io.dropwizard.Configuration;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import org.glassfish.hk2.api.ServiceLocator;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class GuiceApplication<T extends Configuration> extends Application<T> {

  private static final Logger logger = LoggerFactory.getLogger(GuiceApplication.class);

  private final Reflections reflections;
  private final List<Module> appModules = Lists.newArrayList();
  private Injector injector;

  protected GuiceApplication(String... basePackages) {
    final ConfigurationBuilder confBuilder = new ConfigurationBuilder();
    final FilterBuilder filterBuilder = new FilterBuilder();

    if (basePackages.length == 0) {
      basePackages = new String[] {};
    }

    logger.info("op=create, auto_scan_packages={}", (Object[]) basePackages);

    for (String pkg : basePackages) {
      confBuilder.addUrls(ClasspathHelper.forPackage(pkg));
      filterBuilder.include(FilterBuilder.prefix(pkg));
    }

    confBuilder.filterInputsBy(filterBuilder)
        .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner());

    this.reflections = new Reflections(confBuilder);
  }

  @Override
  public void initialize(Bootstrap<T> bootstrap) {
    super.initialize(bootstrap);
  }

  /**
   * When the application runs, this is called after the {@link Bundle}s
   * are run.<p>
   *
   * @param configuration the parsed {@link Configuration} object
   * @param environment the application's {@link Environment}
   * @throws Exception if something goes wrong
   */
  @Override
  public void run(T configuration, Environment environment) throws Exception {
    injector = configureGuice(configuration, environment);
    applicationOnRun(configuration, environment, injector);
  }

  /**
   * Supply a list of modules to be used by Guice. Used by application subclasses.<p>
   *
   * @param configuration the app configuration
   * @return a list of modules to be provisioned by Guice
   */
  abstract protected List<Module> addModules(T configuration, Environment environment);

  /**
   * Access the Dropwizard {@link Environment} and/or the Guice {@link Injector}. This provides
   * a hook for namespaces to add providers and resources for the application as an alternative to
   * accessing {@link #run} in a default Dropwizard app.
   *
   * @param configuration the app configuration
   * @param environment the Dropwizard {@link Environment}
   * @param injector the Guice {@link Injector}
   */
  protected void applicationOnRun(
      T configuration, Environment environment, Injector injector) {
  }

  public Injector injector() {
    return injector;
  }

  private Injector configureGuice(T configuration, Environment environment) throws Exception {
    // setup our core modules...
    appModules.add(new MetricRegistryModule(environment.metrics()));
    appModules.add(new ServletModule());
    // ...and add the app's modules
    appModules.addAll(addModules(configuration, environment));

    final Injector injector = Guice.createInjector(ImmutableList.copyOf(this.appModules));

    // Taken from https://github.com/Squarespace/jersey2-guice/wiki#complex-example. HK2 is no fun.
    JerseyGuiceUtils.install((name, parent) -> {
      if (!name.startsWith("__HK2_Generated_")) {
        return null;
      }

      return injector.createChildInjector(Lists.newArrayList(new JerseyGuiceModule(name)))
          .getInstance(ServiceLocator.class);
    });

    injector.injectMembers(this);
    registerWithInjector(environment, injector);
    return injector;
  }

  private void registerWithInjector(Environment environment, Injector injector) throws Exception {
    registerHealthChecks(environment, injector);
    registerProviders(environment, injector);
    registerResources(environment, injector);
    registerTasks(environment, injector);
    registerManaged(environment, injector);
  }

  private void registerManaged(Environment environment, Injector injector) {
    reflections.getSubTypesOf(Managed.class).forEach(c -> {
      environment.lifecycle().manage(injector.getInstance(c));
    });
  }

  private void registerTasks(Environment environment, Injector injector) {
    reflections.getSubTypesOf(Task.class).forEach(c -> {
      environment.admin().addTask(injector.getInstance(c));
    });
  }

  private void registerHealthChecks(Environment environment, Injector injector) {
    reflections.getSubTypesOf(HealthCheck.class).forEach(c -> {
      environment.healthChecks().register(c.getSimpleName(), injector.getInstance(c));
    });
  }

  private void registerProviders(Environment environment, Injector injector) {
    reflections.getTypesAnnotatedWith(Provider.class).forEach(c -> {
      environment.jersey().register(injector.getInstance(c));
    });
  }

  private void registerResources(Environment environment, Injector injector) {
    reflections.getTypesAnnotatedWith(Path.class).forEach(c -> {
      environment.jersey().register(injector.getInstance(c));
    });
  }
}
