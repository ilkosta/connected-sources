package org.connected_sources.notification.incident;

import org.connected_sources.notification.config.RedmineProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class RedmineTicketLocator {

  private final RestTemplate rest;
  private final int projectId;
  private final int trackerId;

  public RedmineTicketLocator(RedmineProperties props, RestTemplateBuilder builder) {
    this.projectId = props.getProjectId();
    this.trackerId = props.getIncident().getTrackerId();
    this.rest = builder
            .rootUri(props.getBaseUrl())
            .defaultHeader("X-Redmine-API-Key", props.getApiKey())
            .build();
  }

  // Explicit API
  public Optional<String> findOpenTicket(int projectId, int trackerId, String category) {
    return doFindOpenTicket(projectId, trackerId, category);
  }

  public String createTicket(int projectId, int trackerId, String category,
                             String subject, String description, String correlationId) {
    return doCreateTicket(projectId, trackerId, category, subject, description, correlationId);
  }

  // Convenience shortcuts (use configured project/tracker)
  public Optional<String> findOpenTicket(String category) {
    return doFindOpenTicket(this.projectId, this.trackerId, category);
  }
  public String createTicket(String category, String subject, String description, String correlationId) {
    return doCreateTicket(this.projectId, this.trackerId, category, subject, description, correlationId);
  }

  @SuppressWarnings("unchecked")
  private Optional<String> doFindOpenTicket(int projectId, int trackerId, String category) {
    Integer categoryId = resolveCategoryId(projectId, category).orElse(null);
    if (categoryId == null) return Optional.empty();

    String url = String.format("/issues.json?project_id=%d&tracker_id=%d&status_id=open&category_id=%d",
                               projectId, trackerId, categoryId);

    ResponseEntity<Map> resp = rest.getForEntity(url, Map.class);
    var body = resp.getBody();
    if (body == null) return Optional.empty();

    var issues = (List<Map<String,Object>>) body.getOrDefault("issues", List.of());
    if (issues.isEmpty()) return Optional.empty();
    return Optional.of(String.valueOf(issues.get(0).get("id")));
  }

  @SuppressWarnings("unchecked")
  private String doCreateTicket(int projectId, int trackerId, String category,
                                String subject, String description, String correlationId) {
    Integer categoryId = resolveCategoryId(projectId, category).orElse(null);

    Map<String,Object> payload = Map.of(
            "issue", Map.of(
                    "project_id", projectId,
                    "tracker_id", trackerId,
                    "subject", subject,
                    "description", description + "\n\ncorrelationId=" + correlationId,
                    "category_id", categoryId
                           )
                                       );

    ResponseEntity<Map> resp = rest.postForEntity("/issues.json", payload, Map.class);
    var body = resp.getBody();
    if (body == null) throw new IllegalStateException("No response from Redmine");

    Map<String,Object> issue = (Map<String,Object>) body.get("issue");
    return String.valueOf(issue.get("id"));
  }

  @SuppressWarnings("unchecked")
  private Optional<Integer> resolveCategoryId(int projectId, String categoryName) {
    if (categoryName == null || categoryName.isBlank()) return Optional.empty();

    ResponseEntity<Map> resp = rest.getForEntity(
            "/projects/{id}/issue_categories.json", Map.class, projectId);

    var body = resp.getBody();
    if (body == null) return Optional.empty();

    var cats = (List<Map<String,Object>>) body.getOrDefault("issue_categories", List.of());
    return cats.stream()
               .filter(m -> categoryName.equalsIgnoreCase((String)m.get("name")))
               .map(m -> (Integer) m.get("id"))
               .findFirst();
  }
}
