package org.connected_sources.core.user.async;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.async.ops")
public class OpsAsyncProperties {

  @Valid
  private final ExecutorProps executor = new ExecutorProps();

  @Valid
  private final SchedulerProps scheduler = new SchedulerProps();

  public ExecutorProps getExecutor() { return executor; }
  public SchedulerProps getScheduler() { return scheduler; }

  @Validated
  public static class ExecutorProps {
    @Min(1) private int corePoolSize = 8;
    @Min(1) private int maxPoolSize  = 32;
    @Min(0) private int queueCapacity = 500;
    @NotBlank private String threadNamePrefix = "ops-async-";

    @AssertTrue(message = "maxPoolSize deve essere >= corePoolSize")
    public boolean isSizesValid() { return maxPoolSize >= corePoolSize; }

    public int getCorePoolSize() { return corePoolSize; }
    public void setCorePoolSize(int v) { corePoolSize = v; }
    public int getMaxPoolSize() { return maxPoolSize; }
    public void setMaxPoolSize(int v) { maxPoolSize = v; }
    public int getQueueCapacity() { return queueCapacity; }
    public void setQueueCapacity(int v) { queueCapacity = v; }
    public String getThreadNamePrefix() { return threadNamePrefix; }
    public void setThreadNamePrefix(String v) { threadNamePrefix = v; }
  }

  @Validated
  public static class SchedulerProps {
    @Min(1) private int poolSize = 4;
    @NotBlank private String threadNamePrefix = "ops-sched-";

    public int getPoolSize() { return poolSize; }
    public void setPoolSize(int v) { poolSize = v; }
    public String getThreadNamePrefix() { return threadNamePrefix; }
    public void setThreadNamePrefix(String v) { threadNamePrefix = v; }
  }
}
