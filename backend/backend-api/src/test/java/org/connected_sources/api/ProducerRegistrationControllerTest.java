package org.connected_sources.api;

import org.connected_sources.user.ProducerRegistration;
import org.connected_sources.testconfig.WebTestConfig;
import org.connected_sources.shared.User;
import org.connected_sources.user.ProducerService;
import org.connected_sources.user.RegistrationExpiredException;
import org.connected_sources.user.RegistrationNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ActiveProfiles("test")
@ContextConfiguration(classes = WebTestConfig.class)
@WebMvcTest(ProducerRegistrationController.class)
@Import(ProducerRegistrationControllerTest.TestConfig.class)
class ProducerRegistrationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ProducerService producerService;

  private ProducerRegistration registration;

  @BeforeEach
  void setUp() {
    registration = new ProducerRegistration(
            UUID.randomUUID().toString(),
            "test-producer",
            "Test Producer",
            "test@example.com",
            "Via Roma, 1",
            Instant.now()
    );

    when(producerService.register(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(registration);
  }

  @Test
  void testRegisterProducer() throws Exception {
    String requestBody = "{\"name\": \"Test Producer\", \"institutionalEmail\": \"test@example.com\", \"legalHeadquarters\": \"Via Roma, 1\"}";

    mockMvc.perform(post("/api/producer/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
           .andExpect(status().isOk())
           .andExpect(content().json("{\"producerId\": \"test-producer\", \"registrationId\": \"" + registration.getRegistrationId() + "\"}"));
  }

  @Test
  void testRegistrationNotFound() throws Exception {
    Mockito.doThrow(new RegistrationNotFoundException())
           .when(producerService).completeRegistration(Mockito.anyString(), Mockito.anyString(), any(User.class));

    mockMvc.perform(post("/api/producer/missing/complete-registration/invalid-id")
                            .header("X-User-Id", "u1")
                            .header("X-User-Email", "u1@example.com"))
           .andExpect(status().isNotFound())
           .andExpect(content().string("Registration not found"));
  }

  @Test
  void testRegistrationExpired() throws Exception {
    Mockito.doThrow(new RegistrationExpiredException())
           .when(producerService).completeRegistration(Mockito.anyString(), Mockito.anyString(), any(User.class));

    mockMvc.perform(post("/api/producer/expired/complete-registration/reg-id")
                            .header("X-User-Id", "u2")
                            .header("X-User-Email", "u2@example.com"))
           .andExpect(status().isGone())
           .andExpect(content().string("Registration request expired"));
  }

  @Test
  void testMismatchedProducerId() throws Exception {
    Mockito.doThrow(new IllegalArgumentException("Mismatched producer ID"))
           .when(producerService).completeRegistration(Mockito.anyString(), Mockito.anyString(), any(User.class));

    mockMvc.perform(post("/api/producer/wrong-id/complete-registration/valid-id")
                            .header("X-User-Id", "u3")
                            .header("X-User-Email", "u3@example.com"))
           .andExpect(status().isBadRequest())
           .andExpect(content().string("Mismatched producer ID"));
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    public ProducerService producerService() {
      return Mockito.mock(ProducerService.class);
    }
  }
}
