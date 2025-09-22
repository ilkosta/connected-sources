package org.connected_sources.notification.incident;


import org.connected_sources.notification.config.RedmineProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class YamlCategoryResolverTest {

  @Test
  void resolvesFromMapWithDefault() {
    var props = new RedmineProperties();
    var incident = new RedmineProperties.Incident();
    var cat = new RedmineProperties.Category();
    cat.setMap(Map.of(
            "SMTP_550", "Mail Delivery",
            "TELEGRAM_BOT_BLOCKED", "Telegram Delivery",
            "DEFAULT", "Uncategorized"
                     ));
    incident.setCategory(cat);
    props.setIncident(incident);

    var resolver = new YamlCategoryResolver(props);

    assertThat(resolver.resolve("SMTP_550")).contains("Mail Delivery");
    assertThat(resolver.resolve("NON_EXISTENT")).contains("Uncategorized");
    assertThat(resolver.resolve(null)).contains("Uncategorized");
  }
}
