package org.connected_sources.tenant;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TenantContextFilterTest {

  @Test
  void filterSetsAndClearsTenantId() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Tenant-ID", "tenant99");

    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    TenantContextHolder holder = new TenantContextHolder();
    TenantContextFilter filter = new TenantContextFilter(holder);

    filter.doFilter(request, response, chain);

    assertNull(holder.getTenantId()); // cleared after chain.doFilter
    verify(chain).doFilter(request, response);
  }

}
