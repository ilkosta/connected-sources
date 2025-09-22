package org.connected_sources.core.user.async;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.connected_sources.shared.async.ContextAwareTaskDecorator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/* DESIGN
 * -----------------
 * Context-aware executor:
 *  - ContextAwareTaskDecorator captures {tenantId,userId,correlationId}
 *    and populates MDC for child threads.
 *  - Bounded queue + rejection policy (caller-runs) to protect API latency.
 *  - Named threads for easier log correlation.
 */
@Configuration
@EnableConfigurationProperties(OpsAsyncProperties.class)
public class AsyncConfig {

  @Bean(name = "opsExecutor")
  public Executor opsExecutor(OpsAsyncProperties props) {
    var p = props.getExecutor();
    ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
    ex.setCorePoolSize(p.getCorePoolSize());
    ex.setMaxPoolSize(p.getMaxPoolSize());
    ex.setQueueCapacity(p.getQueueCapacity());
    ex.setThreadNamePrefix(p.getThreadNamePrefix());
    ex.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    ex.setTaskDecorator(new ContextAwareTaskDecorator());
    ex.initialize();
    return ex;
  }

  @Bean
  public ThreadPoolTaskScheduler taskScheduler(OpsAsyncProperties props) {
    var s = props.getScheduler();
    ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
    ts.setPoolSize(s.getPoolSize());
    ts.setThreadNamePrefix(s.getThreadNamePrefix());
    ts.setTaskDecorator(new ContextAwareTaskDecorator());
    ts.initialize();
    return ts;
  }
}
