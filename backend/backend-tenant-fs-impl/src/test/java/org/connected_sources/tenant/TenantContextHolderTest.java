package org.connected_sources.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextHolderTest {

  @Test
  void testSetAndClearTenantId() {
    TenantContextHolder holder = new TenantContextHolder();
    assertFalse(holder.isSet());
    holder.setTenantId("tenantA");
    assertEquals("tenantA", holder.getTenantId());
    assertTrue(holder.isSet());
    holder.clear();
    assertFalse(holder.isSet());
  }

  @Test
  void testNoTenantIdDoesNotSetContext() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    ServletResponse response = Mockito.mock(ServletResponse.class);
    FilterChain chain = Mockito.mock(FilterChain.class);

    Mockito.when(request.getHeader("X-Tenant-ID")).thenReturn(null);

    TenantContextHolder holder = new TenantContextHolder();
    TenantContextFilter filter = new TenantContextFilter(holder);

    filter.doFilter(request, response, chain);

    assertFalse(holder.isSet()); // 🧪 Nessun tenant impostato
  }
}
