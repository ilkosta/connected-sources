package org.connected_sources.api.e2e;

import org.connected_sources.api.BackendApiApplication;

import org.connected_sources.config.TestSecurityConfig;
import org.connected_sources.tenant.TenantContextFilter;
import org.connected_sources.tenant.TenantContextHolder;
import org.connected_sources.testconfig.DebugConfig;
import org.connected_sources.testconfig.StubBeans;
import org.connected_sources.testconfig.TenantFsBeanConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.connected_sources.testconfig.DebugConfig;
import org.connected_sources.user.ProducerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.jsonPath;

import org.connected_sources.tenant.TenantContextHolder;
import org.connected_sources.testconfig.WebTestConfig;


@SpringBootTest(
        classes = {
                BackendApiApplication.class,
//                TenantDebugController.class,
                TenantFsBeanConfig.class,
                TenantContextFilter.class,
                StubBeans.class,
                DebugConfig.class,  // include the filter registration
                TestSecurityConfig.class // senza CSRF nei test..
        },
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
//                "spring.security.enabled=false"
        }
)
@Import({TenantContextFilter.class})
@AutoConfigureMockMvc
class TenantDatasourceResolutionE2ETest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldExposeTenantContext() throws Exception {
    mockMvc.perform(get("/api/debug/tenant")
                            .header("X-Producer-Id", "tenant-e2e-01")
//                            .with(user("test").roles("ADMIN"))
                   )
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.tenantId").value("tenant-e2e-01"));
  }

  @Test
  void shouldReturn400WhenNoProducerIdHeaderPresent() throws Exception {
    mockMvc.perform(post("/api/test/ping")
//                            .with(csrf() )
                   )
           .andExpect(status().isBadRequest())
           .andExpect(content().string(containsString("Tenant not set")));
  }

  @Test
  void shouldProvisionAndRespondForNewTenant() throws Exception {
    mockMvc.perform(post("/api/test/ping")
                            .header("X-Producer-Id", "new-producer-001")
//                            .with(user("test").roles("ADMIN"))
                   )
           .andExpect(status().isOk())
           .andExpect(content().string("pong new-producer-001"));
  }

  @Test
  void shouldRouteToCorrectTenantDb() throws Exception {
    mockMvc.perform(post("/api/test/ping")
                            .header("X-Producer-Id", "tenant-A")
//                            .with(user("test").roles("ADMIN"))
                   )
           .andExpect(status().isOk())
           .andExpect(content().string("pong tenant-A"));

    mockMvc.perform(post("/api/test/ping")
                            .header("X-Producer-Id", "tenant-B"))
           .andExpect(status().isOk())
           .andExpect(content().string("pong tenant-B"));
  }
}

//
////@Import(WebTestConfig.class)
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//@Import({DebugConfig.class})
//class TenantDatasourceResolutionE2ETest {
//
//  @Autowired
//  private MockMvc mockMvc;
//
//  @MockBean
//  ProducerService producerService;
//
//  @Autowired
//  TenantContextHolder tenantContextHolder;
//
//
//  /** debug call to verify that the tenant resolution is working in test env */
//  @Test
//  void shouldExposeTenantContext() throws Exception {
//    mockMvc.perform(get("/api/debug/tenant")
//            .header("X-Producer-Id", "tenant-e2e-01"))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.tenantId").value("tenant-e2e-01"));
//  }
//
//
//
//  @Test
//  void shouldReturn400WhenNoProducerIdHeaderPresent() throws Exception {
//    mockMvc.perform(post("/api/test/ping"))
//           .andExpect(status().isBadRequest())
//           .andExpect(content().string(containsString("Tenant not set")));
//  }
//
//  @Test
//  void shouldProvisionAndRespondForNewTenant() throws Exception {
//    mockMvc.perform(post("/api/test/ping")
//                            .header("X-Producer-Id", "new-producer-001"))
//           .andExpect(status().isOk())
//           .andExpect(content().string("pong"));
//  }
//
//  @Test
//  void shouldRouteToCorrectTenantDb() throws Exception {
//    mockMvc.perform(post("/api/test/ping")
//                            .header("X-Producer-Id", "tenant-A"))
//           .andExpect(status().isOk())
//           .andExpect(content().string("pong"));
//
//    mockMvc.perform(post("/api/test/ping")
//                            .header("X-Producer-Id", "tenant-B"))
//           .andExpect(status().isOk())
//           .andExpect(content().string("pong"));
//  }
//}
