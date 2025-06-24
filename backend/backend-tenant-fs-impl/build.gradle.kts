plugins {
    id("java-library")
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
}



dependencies {
    // Core di Spring (NO spring-boot!)
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("jakarta.servlet:jakarta.servlet-api:6.1.0")
    // Moduli interni
    implementation(project(":backend-shared"))
    implementation(project(":backend-tenant-api"))

    // Dipendenze di test
//    testImplementation("org.springframework:spring-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // https://mvnrepository.com/artifact/org.mockito/mockito-junit-jupiter
    testImplementation("org.mockito:mockito-junit-jupiter:5.18.0")
//    testImplementation("org.mockito:mockito-inline") // Enable mocking of final classes/methods
    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:5.18.0")

    // https://mvnrepository.com/artifact/jakarta.servlet/jakarta.servlet-api
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")
}
