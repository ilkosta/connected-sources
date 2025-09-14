package org.connected_sources.tenantdb;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tenant.sqlite.flyway")
public class TenantMigrationProperties {
    private boolean enabled = true;
    private String schemaPrefix = "tenant_";
    private String baselineVersion = "1.0";
    private String[] locations;
    private boolean validateOnMigrate = true;
    private String historyTable = "history_table";
    private boolean baselineOnMigrate = false;

    public boolean isBaselineOnMigrate() {
        return baselineOnMigrate;
    }

    public void setBaselineOnMigrate(boolean baselineOnMigrate) {
        this.baselineOnMigrate = baselineOnMigrate;
    }

    public String getHistoryTable() {
        return historyTable;
    }

    public void setHistoryTable(String historyTable) {
        this.historyTable = historyTable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSchemaPrefix() {
        return schemaPrefix;
    }

    public void setSchemaPrefix(String schemaPrefix) {
        this.schemaPrefix = schemaPrefix;
    }

    public String getBaselineVersion() {
        return baselineVersion;
    }

    public void setBaselineVersion(String baselineVersion) {
        this.baselineVersion = baselineVersion;
    }

    public String[] getLocations() {
        return locations;
    }

    public void setLocations(String[] locations) {
        this.locations = locations;
    }

    public boolean isValidateOnMigrate() {
        return validateOnMigrate;
    }

    public void setValidateOnMigrate(boolean validateOnMigrate) {
        this.validateOnMigrate = validateOnMigrate;
    }
}