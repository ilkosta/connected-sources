package org.connected_sources.tenant.fs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FsTenantDatasourceRegistryTest {

  private FsTenantDatasourceResolver resolver;
  private FsTenantDatasourceRegistry registry;

  @BeforeEach
  void setUp() {
    resolver = mock(FsTenantDatasourceResolver.class);
    registry = new FsTenantDatasourceRegistry(resolver);
  }

  @Test
  void shouldReturnExistingDataSourceFromCache() {
    DataSource ds = mock(DataSource.class);
    registry.registerDataSource("tenantA", ds);

    DataSource result = registry.getDataSource("tenantA");
    assertSame(ds, result);
  }

  @Test
  void shouldCreateAndCacheDataSourceIfNotPresent() {
    DataSource ds = mock(DataSource.class);
    when(resolver.createDataSource("tenantB")).thenReturn(ds);

    DataSource result1 = registry.getDataSource("tenantB");
    DataSource result2 = registry.getDataSource("tenantB");

    assertSame(ds, result1);
    assertSame(result1, result2);
    verify(resolver, times(1)).createDataSource("tenantB");
  }

  @Test
  void shouldThrowOnNullTenantId() {
    assertThrows(IllegalArgumentException.class, () -> registry.getDataSource(null));
  }

  @Test
  void shouldThrowOnBlankTenantId() {
    assertThrows(IllegalArgumentException.class, () -> registry.getDataSource("   "));
  }

  @Test
  void shouldThrowOnNullRegistrationInput() {
    DataSource ds = mock(DataSource.class);
    assertThrows(IllegalArgumentException.class, () -> registry.registerDataSource(null, ds));
    assertThrows(IllegalArgumentException.class, () -> registry.registerDataSource("tenantX", null));
  }
}
