package org.connected_sources.shared.naming;

public final class TenantIdNormalizer {
  private TenantIdNormalizer() {}

  /* FUNCTION
   * normalize(raw)
   * --------------
   * Returns a filesystem/URL-safe tenantId:
   *  - lowercase, [a-z0-9-_], spaces collapsed to '-'
   *  - rejects empty or all-non-word inputs
   * Invariant: normalize(x) is idempotent and stable across versions.
   */
  // WHY 
  // Keeping normalization semantics stable ensures existing FS paths
  // remain valid; changes would require a migration plan.
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
