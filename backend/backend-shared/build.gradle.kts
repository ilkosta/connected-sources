plugins {
    id("java-library")
    id("io.spring.dependency-management") version "1.1.7"
}

dependencies {
    // Core di Spring (NO spring-boot!)
    implementation("org.springframework:spring-context")
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.springframework:spring-tx")

    // Dipendenze di test
    testImplementation("org.springframework:spring-test")
}