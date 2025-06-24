package org.connected_sources.tenant;

/**
 * Interfaccia per risolvere dinamicamente il path o l'identificativo del datasource
 * associato a un tenant specifico.
 */
public interface TenantDatasourceResolver {

  /**
   * Restituisce il percorso assoluto del file/datasource associato al tenant.
   *
   * @param tenantId identificativo univoco del tenant
   * @return path assoluto al file SQLite, o stringa con URL di connessione
   */
  String resolvePathForTenant(String tenantId);
}
