package org.connected_sources.api.web;

import java.io.IOException;
import java.util.Optional;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.connected_sources.shared.context.TenantContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.connected_sources.shared.context.TenantContextHolder;

/*
    resolve tenant/user & correlation
 */
@Component
@Order(1)
public class TenantResolutionFilter implements Filter {
  @Override public void doFilter(ServletRequest request, ServletResponse response, @NotNull FilterChain chain)
          throws IOException, ServletException {
    TenantContext tenantContext = TenantContextHolder.get();
    if(tenantContext == null) {
        chain.doFilter(request, response);
    }
    else if(tenantContext.correlationId().isEmpty()) {
      HttpServletRequest req = (HttpServletRequest) request;
      String tenantId = Optional.ofNullable(req.getHeader("X-Tenant-Id")).orElse("default");
      Long userId = Optional.ofNullable(req.getHeader("X-User-Id")).map(Long::valueOf).orElse(-1L);
      String corr = req.getHeader("X-Correlation-Id");
      try {
        TenantContextHolder.set(TenantContextHolder.from(tenantId, userId, corr));
        chain.doFilter(request, response);
      } finally { TenantContextHolder.clear(); }
    } else {
      chain.doFilter(request, response);
    }
  }
}