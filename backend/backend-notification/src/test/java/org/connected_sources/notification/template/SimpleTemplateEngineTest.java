package org.connected_sources.notification.template;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class SimpleTemplateEngineTest {

    @Test
    void rendersVariablesAndDefaults() {
        var eng = new SimpleTemplateEngine();
        var tpl = """
      # Hi {{name}}
      Email: {{email|n/a}}
      Literal: \\{{doNotExpand}}
      Missing: {{missing}}
      """;
        var out = eng.render(tpl, Map.of("name","Ada","email","ada@ex"));
        assertThat(out).contains("Hi Ada");
        assertThat(out).contains("Email: ada@ex");
        assertThat(out).contains("Literal: {{doNotExpand}}");
        assertThat(out).contains("Missing: ");
    }
}
