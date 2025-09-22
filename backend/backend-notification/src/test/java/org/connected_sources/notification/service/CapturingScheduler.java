package org.connected_sources.notification.service;

import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class CapturingScheduler implements TaskScheduler {
  public static class Task {
    public final Runnable runnable;
    public final Instant when;
    Task(Runnable r, Instant t) { this.runnable = r; this.when = t; }
  }
  public final List<Task> tasks = new ArrayList<>();

  @Override public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
    tasks.add(new Task(task, startTime));
    return null;
  }

  // Unused overloads
  @Override public ScheduledFuture<?> schedule(Runnable task, org.springframework.scheduling.Trigger trigger) { return null; }
  @Override public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) { return null; }
  @Override public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) { return null; }
  @Override public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) { return null; }
  @Override public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) { return null; }
}
