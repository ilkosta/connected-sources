plugins {
    id("java-library")
    id("io.spring.dependency-management") version "1.1.7"
}



dependencies {
    // Core di Spring (NO spring-boot!)
    implementation("org.springframework:spring-context")
    // https://mvnrepository.com/artifact/jakarta.servlet/jakarta.servlet-api
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Moduli interni
    implementation(project(":backend-shared"))
    implementation(project(":backend-tenant-api"))

    // Dipendenze di test
    testImplementation("org.springframework:spring-test")
}
