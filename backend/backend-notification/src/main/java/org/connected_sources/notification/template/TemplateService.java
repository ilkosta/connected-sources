package org.connected_sources.notification.template;

import org.connected_sources.notification.template.TemplateRepository.StoredTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TemplateService {

    private final TemplateRepository repo;
    private final SimpleTemplateEngine engine;

    public TemplateService(TemplateRepository repo, SimpleTemplateEngine engine) {
        this.repo = repo;
        this.engine = engine;
    }

    /**
     * Load template by name (latest or specific version) and inject variables.
     * Throws IllegalArgumentException if template is missing (fail-fast).
     */
    public RenderedTemplate render(String name, Map<String, Object> variables, Integer version) {
        StoredTemplate t = repo.findByNameAndVersion(name, version)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + name + (version != null ? " v" + version : "")));
        String body = engine.render(t.bodyMd(), variables);
        return new RenderedTemplate(t.name(), t.version(), body);
    }

    public record RenderedTemplate(String name, int version, String bodyMd) {}
}
