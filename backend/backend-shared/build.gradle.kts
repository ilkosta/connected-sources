plugins {
    id("java-library")
    id("io.spring.dependency-management") version "1.1.7"
}

dependencies {
    // Core di Spring (NO spring-boot!)
    implementation("org.springframework:spring-context")

    // Dipendenze di test
    testImplementation("org.springframework:spring-test")
}