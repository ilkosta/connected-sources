package org.connected_sources.tenant.spi;

import java.io.IOException;
import java.nio.file.Path;

public interface TenantLogLocator {
  Path logsDir(String tenantId) throws IOException;
}
