package org.connected_sources.notification.template;


import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple, dependency-free renderer:
 *  - Replaces {{key}} with variables.get("key")
 *  - Supports default: {{key|Default Text}}
 *  - Leaves unknown keys empty string if no default
 *  - Escaping: use \{{ to render a literal '{{'
 */
@Component
public class SimpleTemplateEngine {

    // Matches {{ key }} or {{ key|Default Text }}
    private static final Pattern P = Pattern.compile("\\\\?\\{\\{\\s*([a-zA-Z0-9_\\.\\-]+)(?:\\|(.*?))?\\s*}}");

    public String render(String template, Map<String, Object> variables) {
        Matcher m = P.matcher(template);
        StringBuffer out = new StringBuffer(template.length() + 64);

        while (m.find()) {
            String whole = m.group(0);
            boolean escaped = whole.startsWith("\\{{");

            if (escaped) { // \{{...}} -> output {{...}} without expansion
                m.appendReplacement(out, Matcher.quoteReplacement(whole.substring(1)));
                continue;
            }

            String key = m.group(1);
            String def = m.group(2); // null if not default
            Object val = variables.get(key);
            String replacement = (val != null) ? String.valueOf(val) : (def != null ? def : "");

            m.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(out);
        return out.toString();
    }
}

