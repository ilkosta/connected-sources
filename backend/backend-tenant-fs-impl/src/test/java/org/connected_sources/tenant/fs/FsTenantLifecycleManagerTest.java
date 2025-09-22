package org.connected_sources.tenant.fs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class FsTenantLifecycleManagerTest {

    private FsTenantDatasourceResolver resolver;
    private FsTenantDatasourceRegistry registry;
    private FsTenantLifecycleManager lifecycleManager;

    @BeforeEach
    void setUp() {
        resolver = mock(FsTenantDatasourceResolver.class);
        registry = mock(FsTenantDatasourceRegistry.class);
        lifecycleManager = new FsTenantLifecycleManager(resolver, registry, "./tenants");
    }

    @Test
    void shouldProvisionAndRegisterTenantDataSource() {
        String tenantId = "tenant123";
        DataSource mockDs = mock(DataSource.class);

        when(resolver.createDataSource(tenantId, null)).thenReturn(mockDs);

        lifecycleManager.provisionTenant(tenantId);

        verify(resolver).createDataSource(tenantId, null);
        verify(registry).registerDataSource(tenantId, mockDs);
    }

    @Test
    void shouldThrowIfTenantIdIsNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> lifecycleManager.provisionTenant(null));
        assertThrows(IllegalArgumentException.class, () -> lifecycleManager.provisionTenant("  "));
    }
}
