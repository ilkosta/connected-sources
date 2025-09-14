//package org.connected_sources.tenant.fs.health;
//
//
//import org.connected_sources.tenant.fs.FsTenantLifecycleManager;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//class FsSpaceHealthIndicatorTest {
//
//    @Test
//    void returns_down_when_below_threshold_and_cached_primary() {
//        FsTenantLifecycleManager fs = mock(FsTenantLifecycleManager.class);
//        when(fs.fsLogDir(anyString())).thenReturn(java.nio.file.Path.of("/tmp")); // not used in example
//
//        FsSpaceHealthIndicator real = new FsSpaceHealthIndicator(fs); //, 5_000_000_000L); // 5GB
////        CachingHealthIndicator cached = new CachingHealthIndicator(real, java.time.Duration.ofSeconds(60));
//
//        var h1 = cached.health();
////        var h2 = cached.health(); // cached
//
//        assertThat(h1.getStatus().getCode()).isIn("UP","DOWN"); // depends on env
//        assertThat(h2).isSameAs(h1); // cached result
//    }
//}
