package org.connected_sources.tenant;

import org.springframework.stereotype.Component;

@Component
public class TenantContextHolder {

//  private TenantContextHolder() {
//    throw new IllegalStateException("Utility class");
//  }

  private final ThreadLocal<String> context = new ThreadLocal<>();

  public void setTenantId(String tenantId) {
    context.set(tenantId);
  }

  public String getTenantId() {
    return context.get();
  }

  public void clear() {
    context.remove();
  }

  public boolean isSet() {
    return context.get() != null;
  }
}
