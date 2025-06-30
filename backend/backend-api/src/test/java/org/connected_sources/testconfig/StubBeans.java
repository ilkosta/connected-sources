package org.connected_sources.testconfig;

import org.connected_sources.user.ProducerService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class StubBeans {

  @Bean
  public ProducerService producerService() {
    return mock(ProducerService.class);
  }
}