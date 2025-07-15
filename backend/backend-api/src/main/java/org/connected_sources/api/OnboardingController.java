package org.connected_sources.api;

import jakarta.validation.Valid;
import org.connected_sources.api.dto.OnboardingRequest;
import org.connected_sources.user.CreateUserAndProducerCommand;
import org.connected_sources.user.OnboardingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/onboard")
public class OnboardingController {

  private final OnboardingService onboardingService;

  public OnboardingController(OnboardingService onboardingService) {
    this.onboardingService = onboardingService;
  }

  @PostMapping
  public ResponseEntity<Void> onboard(@Valid @RequestBody OnboardingRequest request) {
    var command = new CreateUserAndProducerCommand(
            request.getProducerName(),
            request.getAdminEmail()
    );

    onboardingService.handleOnboarding(command);
    return ResponseEntity.ok().build();
  }
}