package org.connected_sources.tenant.fs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FsTenantDatasourceResolverTest {
    @Mock
    private FsTenantDatasourceResolver datasourceResolver;

    @InjectMocks
    private FsTenantDatasourceRegistry registry;

    @Test
    void shouldReturnCachedDataSource() {
        DataSource ds = mock(DataSource.class);
        registry.registerDataSource("tenant1", ds);

        DataSource result = registry.getDataSource("tenant1");
        assertSame(ds, result);
    }

//    @Test
//    void shouldCreateAndRegisterNewDataSource() {
//        DataSource newDs = mock(DataSource.class);
//        when(datasourceResolver.createDataSource("tenant2")).thenReturn(newDs);
//
//        DataSource result = registry.getOrCreateDataSource("tenant2", datasourceResolver);
//
//        assertSame(newDs, result);
//        assertSame(result, registry.getDataSource("tenant2"));
//        verify(datasourceResolver).createDataSource("tenant2");
//    }


//    @Test
//    public void testResolveDatasourcePath() {
//        FsTenantDatasourceResolver resolver = new FsTenantDatasourceResolver("/data/base");
//        String tenantId = "t1";
//
//        String path = resolver.resolvePathForTenant(tenantId);
//        assertEquals("/data/base/t1/datasource.sqlite", path);
//    }
}
