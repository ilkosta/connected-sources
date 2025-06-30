package org.connected_sources.tenant;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component // <-- autodiscover
public class TenantContextFilter implements Filter {

  private final TenantContextHolder tenantContextHolder;

  public TenantContextFilter(TenantContextHolder tenantContextHolder) {
    this.tenantContextHolder = tenantContextHolder;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {
//    try {
//      String tenantId = extractTenantId((HttpServletRequest) request);
//      System.out.println("⛳ Filtering request, setting tenantId");
//      if (tenantId != null && !tenantId.isBlank()) {
//        tenantContextHolder.setTenantId(tenantId);
//        System.out.println("TenantContextFilter → set tenantId = " + tenantId);
//      }
//      chain.doFilter(request, response);
//    } finally {
//      tenantContextHolder.clear(); // cleanup in all cases
//    }

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String tenantId = httpRequest.getHeader("X-Producer-Id");
    System.out.println("⛳ Filtering request, setting tenantId: " + tenantId);
    tenantContextHolder.setTenantId(tenantId);
    chain.doFilter(request, response);
  }

  private String extractTenantId(HttpServletRequest request) {
    // Simplified example: header-based
    return request.getHeader("X-Tenant-ID");
  }
}
