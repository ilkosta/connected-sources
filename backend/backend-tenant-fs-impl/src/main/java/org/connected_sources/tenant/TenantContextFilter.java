package org.connected_sources.tenant;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.connected_sources.tenant.TenantContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TenantContextFilter implements Filter {

  private final TenantContextHolder tenantContextHolder;

  public TenantContextFilter(TenantContextHolder tenantContextHolder) {
    this.tenantContextHolder = tenantContextHolder;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {
    try {
      String tenantId = extractTenantId((HttpServletRequest) request);
      if (tenantId != null && !tenantId.isBlank()) {
        tenantContextHolder.setTenantId(tenantId);
      }
      chain.doFilter(request, response);
    } finally {
      tenantContextHolder.clear(); // cleanup in all cases
    }
  }

  private String extractTenantId(HttpServletRequest request) {
    // Simplified example: header-based
    return request.getHeader("X-Tenant-ID");
  }
}
