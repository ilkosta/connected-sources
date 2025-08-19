package org.connected_sources.shared.context;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import static org.connected_sources.shared.context.MDContext.*;

@Component
public class TenantContextHolder {

  // VIP the use of InheritableThreadLocal to always have the context also in manual created threads (without the use of the decorator)
  private static final ThreadLocal<TenantContext> CTX = new InheritableThreadLocal<>();

  public static void set(TenantContext ctx) {
    CTX.set(ctx);
    if (ctx != null) {
      MDC.put(MDC_TENANT.getString(), ctx.tenantId());
      String dbgTenant = MDC.get(MDC_TENANT.getString()); // for test/debug

      String userIdStr = Optional.ofNullable(ctx.userId())
              .map(String::valueOf)
              .orElse("");
      System.out.println("userIdStr: '" + userIdStr + "'");
      MDC.put(MDC_USER.getString(), userIdStr);
//      MDC.put(MDC_USER.getString(), Optional.ofNullable(ctx.userId()).map(String::valueOf).orElse(""));
      MDC.put(MDC_CORR.getString(), ctx.correlationId());
    }
  }


  public static TenantContext get() {
    return CTX.get();
  }


  public static void clear() {
    CTX.remove();
    MDC.remove(MDC_TENANT.getString());
    MDC.remove(MDC_USER.getString());
    MDC.remove(MDC_CORR.getString());
  }

  public static TenantContext from(String tenantId, Long userId, String correlationId) {
    return new TenantContext(tenantId, userId,
            (correlationId == null || correlationId.isBlank() )
                    ? UUID.randomUUID().toString()
                    : correlationId);
  }
}
