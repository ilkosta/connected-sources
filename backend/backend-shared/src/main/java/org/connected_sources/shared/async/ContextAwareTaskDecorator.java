package org.connected_sources.shared.async;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.connected_sources.shared.context.TenantContextHolder;
import org.connected_sources.shared.context.TenantContext;
import org.springframework.stereotype.Component;

@Component
public class ContextAwareTaskDecorator implements TaskDecorator {
  @Override
  public Runnable decorate(Runnable runnable) {

    TenantContext parentCtx = TenantContextHolder.get();

    Map<String, String> parentMdc = MDC.getCopyOfContextMap();

    return () -> {
      try {
        if (parentCtx != null) TenantContextHolder.set(parentCtx);
        else TenantContextHolder.clear();

        if (parentMdc != null) MDC.setContextMap(parentMdc);
        else MDC.clear();

        runnable.run();
      } finally {
        TenantContextHolder.clear();
      }
    };
  }
}