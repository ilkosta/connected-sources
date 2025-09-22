package org.connected_sources.tenant.fs;

import org.connected_sources.shared.context.TenantContext;
import org.connected_sources.shared.context.TenantContextHolder;
import org.connected_sources.tenant.spi.TenantAwareDataSourceManager;
import org.connected_sources.tenant.spi.TenantDatasourceRegistry;
import org.connected_sources.tenant.spi.TenantLifecycleManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class TenantAwareDataSourceManagerTest {

//  @Mock
  private TenantContextHolder contextHolder;

  @Mock
  private TenantDatasourceRegistry registry;

  @Mock
  private TenantLifecycleManager lifecycleManager;

  @InjectMocks
  private FsTenantAwareDataSourceManager manager;


  private final String tenantId = "tenant42";

  @Mock
  private /*final*/ DataSource mockDataSource; // altrimenti Mockito non riesce ad ignettare usando solo reflection

  @BeforeEach
  void setUp() {
    contextHolder.set(new TenantContext(tenantId,1L, "first :)"));
  }

  @AfterEach
  void tearDown() {
    contextHolder.clear(); // usa ThreadLocal!!
  }

  @Test
  void shouldResolveExistingDataSource() {
    // preparo lo stub del registry
    when(registry.getDataSource(tenantId)).thenReturn(mockDataSource);

    DataSource result = manager.resolveDataSource();

    assertEquals(mockDataSource, result);
    verify(lifecycleManager, never()).provisionTenant(anyString());
  }

  @Test
  void shouldProvisionAndReturnNewDataSource() {
    when(registry.getDataSource(tenantId)).thenReturn(null, mockDataSource);

    DataSource result = manager.resolveDataSource();

    assertEquals(mockDataSource, result);
    verify(lifecycleManager).provisionTenant(tenantId);
  }

//  @Test
//  void shouldThrowIfTenantContextMissing() {
//    IllegalStateException exception = assertThrows(
//            IllegalStateException.class,
//            manager::resolveDataSource
//                                                  );
//
//    assertEquals("Tenant context not set", exception.getMessage());
//  }

//  @Test
//  void shouldThrowIfProvisionFailsToRegisterDatasource() {
//    when(registry.getDataSource(tenantId)).thenReturn(null);
//    doNothing().when(lifecycleManager).provisionTenant(tenantId);
//
//    IllegalStateException ex = assertThrows(IllegalStateException.class, manager::resolveDataSource);
//    assertTrue(ex.getMessage().contains("unavailable"));
//  }
}
