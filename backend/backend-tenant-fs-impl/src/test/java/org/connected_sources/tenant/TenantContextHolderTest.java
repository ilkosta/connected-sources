package org.connected_sources.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextHolderTest {

  private final TenantContextHolder holder = new TenantContextHolder();
  @Test
  void testSetAndClearTenantId() {
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

    TenantContextFilter filter = new TenantContextFilter(holder);

    filter.doFilter(request, response, chain);

    assertFalse(holder.isSet()); // 🧪 Nessun tenant impostato
  }



    @Test
    void shouldStoreAndRetrieveTenantId() {
      holder.setTenantId("tenantA");
      assertEquals("tenantA", holder.getTenantId());
    }

    @Test
    void shouldClearTenantId() {
      holder.setTenantId("tenantA");
      holder.clear();
      assertNull(holder.getTenantId());
    }
}
