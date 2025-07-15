package org.connected_sources.tenant;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TenantContextHolder {

//  private TenantContextHolder() {
//    throw new IllegalStateException("Utility class");
//  }

  private final ThreadLocal<String> context = new ThreadLocal<>();

  public void setTenantId(String tenantId) {
    context.set(tenantId);
  }

  public Optional<String> getTenantId() {
    return Optional.ofNullable(context.get());
  }

  public void clear() {
    context.remove();
  }

  public boolean isSet() {
    return context.get() != null;
  }
}
