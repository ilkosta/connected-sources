package org.connected_sources.tenant;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

public class TenantContextFilter implements Filter {

  public static final String HEADER = "X-Tenant-ID";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {

    try {
      String tenantId = ((HttpServletRequest) request).getHeader(HEADER);
      if (tenantId != null && !tenantId.isBlank()) {
        TenantContextHolder.setTenantId(tenantId);
      }
      chain.doFilter(request, response);
    } finally {
      TenantContextHolder.clear(); // Clean-up always
    }
  }
}
