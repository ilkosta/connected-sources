package org.connected_sources.tenant;

public class TenantContextHolder {

  private TenantContextHolder() {
    throw new IllegalStateException("Utility class");
  }

  private static final ThreadLocal<String> context = new ThreadLocal<>();

  public static void setTenantId(String tenantId) {
    context.set(tenantId);
  }

  public static String getTenantId() {
    return context.get();
  }

  public static void clear() {
    context.remove();
  }

  public static boolean isSet() {
    return context.get() != null;
  }
}
