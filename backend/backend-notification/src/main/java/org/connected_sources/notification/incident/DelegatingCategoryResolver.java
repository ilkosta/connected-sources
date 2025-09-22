package org.connected_sources.notification.incident;

import org.connected_sources.notification.config.RedmineProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Façade over the active CategoryResolver.
 * For now we run in YAML-only mode and just delegate to YamlCategoryResolver.
 *
 * Keeping this class I can flip the strategy later (e.g. DB) without
 * touching callers that depend on CategoryResolver.
 */
@Component
@Primary
public class DelegatingCategoryResolver implements CategoryResolver {

  private static final Logger log = LoggerFactory.getLogger(DelegatingCategoryResolver.class);

  private final YamlCategoryResolver yaml;
  private final boolean hasDefault;

  public DelegatingCategoryResolver(YamlCategoryResolver yaml, RedmineProperties props) {
    this.yaml = yaml;
    // helpful warning if DEFAULT is missing in YAML map
    var map = props.getIncident() != null && props.getIncident().getCategory() != null
            ? props.getIncident().getCategory().getMap()
            : null;
    this.hasDefault = map != null && map.containsKey("DEFAULT");
    if (!hasDefault) {
      log.warn("redmine.incident.category.map is missing a DEFAULT entry; unknown error codes will resolve to empty.");
    }
  }

  @Override
  public Optional<String> resolve(String errorCode) {
    // YAML-only delegation
    var resolved = yaml.resolve(errorCode);
    if (resolved.isEmpty() && !hasDefault) {
      // last-resort guard to avoid null category usage downstream
      return Optional.of("Uncategorized");
    }
    return resolved;
  }
}
