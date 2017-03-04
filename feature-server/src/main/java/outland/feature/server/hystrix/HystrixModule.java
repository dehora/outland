package outland.feature.server.hystrix;

import com.google.inject.AbstractModule;
import com.netflix.hystrix.HystrixCommandProperties;

public class HystrixModule extends AbstractModule {

  @Override
  protected void configure() {
    int higherForDev = 16000;
    boolean trueForDev = true;
    HystrixCommandProperties.Setter()
        .withRequestLogEnabled(trueForDev)
        .withExecutionTimeoutInMilliseconds(higherForDev);
  }
}
