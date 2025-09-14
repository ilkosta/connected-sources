package org.connected_sources.tenant.spi.db;

import org.connected_sources.shared.tenantdb.DataSourceDescriptor;

public interface TenantDescriptorStore {
  DataSourceDescriptor readDescriptor(String tenantId);
}