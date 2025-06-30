plugins {
    id("java-library")
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
}

dependencies {
    // Moduli interni
    implementation(project(":backend-core:backend-core-user"))
    implementation(project(":backend-tenant-api"))
        implementation(project(":backend-shared"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    // https://mvnrepository.com/artifact/org.hibernate.validator/hibernate-validator
    implementation("org.hibernate.validator:hibernate-validator:9.0.1.Final")

    // Dipendenze di test
    testImplementation(project(":backend-tenant-fs-impl"))
    testImplementation("org.springframework:spring-test")
    testImplementation("org.springframework.security:spring-security-test")
    // implementestImplementationtation("org.springframework.boot:spring-boot-starter-test")
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.intuit.karate:karate-junit5:1.4.1")


}


tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("org.connected_sources.api.BackendApiApplication")
}

tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    mainClass.set("org.connected_sources.api.BackendApiApplication")
}
