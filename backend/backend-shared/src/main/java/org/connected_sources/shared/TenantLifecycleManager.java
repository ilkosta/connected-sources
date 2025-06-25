package org.connected_sources.shared;

/**
 * Responsible for provisioning a new tenant, including initialization of
 * folders, databases, and initial DataSource setup.
 */
public interface TenantLifecycleManager {

  /**
   * Ensures the given tenant is provisioned. This method is idempotent and safe to call
   * even if the tenant has already been initialized.
   *
   * @param tenantId the tenant identifier
   */
  void provisionTenant(String tenantId);
}
