package org.connected_sources.notification.incident;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.connected_sources.notification.config.RedmineProperties;

@Component
public class YamlCategoryResolver implements CategoryResolver {
  private final Map<String,String> mapping; // error -> incident category

  public YamlCategoryResolver(RedmineProperties props) {
    this.mapping = props.getIncident().getCategory().getMap();
  }

  @Override
  public Optional<String> resolve(String errorCode) {
    if (errorCode == null) return Optional.ofNullable(mapping.get("DEFAULT"));
    return Optional.ofNullable(mapping.getOrDefault(errorCode, mapping.get("DEFAULT")));
  }
}