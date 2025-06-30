package org.connected_sources.api.e2e;

import com.intuit.karate.junit5.Karate;

class KarateTestRunner {

    @Karate.Test
    Karate testTenantDatasourceResolution() {
        return Karate.run("classpath:features/tenant/tenant-datasource-resolution.feature");
    }

    @Karate.Test
    Karate testTenantOnboardingAndContent() {
        return Karate.run("classpath:features/tenant/tenant-onboarding-and-content.feature");
    }
}
