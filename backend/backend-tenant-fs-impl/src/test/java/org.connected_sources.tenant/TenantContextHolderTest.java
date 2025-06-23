package org.connected_sources.tenant;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TenantContextHolderTest {

  @Test
  void testSetAndClearTenantId() {
    assertFalse(TenantContextHolder.isSet());
    TenantContextHolder.setTenantId("tenantA");
    assertEquals("tenantA", TenantContextHolder.getTenantId());
    assertTrue(TenantContextHolder.isSet());
    TenantContextHolder.clear();
    assertFalse(TenantContextHolder.isSet());
  }
}
