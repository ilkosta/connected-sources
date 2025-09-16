package org.connected_sources.shared.naming;

public final class TenantIdNormalizer {
    private TenantIdNormalizer() {}
    public static String normalize(String raw) {
        if (raw == null) throw new IllegalArgumentException("tenant id source is null");
        // lowercase, trim, replace spaces & invalids with '-', keep [a-z0-9-_]
        String s = raw.trim().toLowerCase();
        s = s.replaceAll("[\\s]+", "-");
        s = s.replaceAll("[^a-z0-9-_]", "-");
        s = s.replaceAll("-{2,}", "-");
        return s.replaceAll("^-|-$", "");
    }
}
