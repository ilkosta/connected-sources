plugins {
    id("java-library")
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
}



dependencies {
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springframework:spring-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql") // let Spring Boot manage the version

    implementation("jakarta.servlet:jakarta.servlet-api:6.1.0")
    implementation("org.realityforge.org.jetbrains.annotations:org.jetbrains.annotations:1.7.0")

    // JSON serialization
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")

    // SLF4J API for logging
    implementation("org.slf4j:slf4j-api:2.0.17")

    // SQLite JDBC driver
    implementation("org.xerial:sqlite-jdbc:3.50.1.0")
    implementation("org.flywaydb:flyway-core:11.13.0")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:11.13.1")

    // Moduli interni
    implementation(project(":backend-shared"))
    implementation(project(":backend-tenant-api"))


    // Dipendenze di test
//    testImplementation("org.springframework:spring-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:5.18.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.18.0")


    // https://mvnrepository.com/artifact/jakarta.servlet/jakarta.servlet-api
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")
}

tasks.named<Jar>("jar") {
    enabled = true
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}