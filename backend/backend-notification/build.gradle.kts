plugins {
    id("java-library")
//    id("org.springframework.boot") version "3.5.0" apply false
    id("io.spring.dependency-management")
}

dependencies {

    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation("org.springframework.boot:spring-boot-starter-web") // RestTemplate

//    implementation(platform("org.springframework.boot:spring-boot-dependencies"))
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.commonmark:commonmark:0.26.0")

    // For @ConfigurationProperties binding
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // for exceptions -> ExceptionUtils.getStackTrace
    implementation("org.apache.commons:commons-lang3:3.20.0")

    // Shared module
    implementation(project(":backend-shared"))
//    implementation(project(":backend-logging-fs-impl"))

//    // --- Test ---
//    testImplementation("org.springframework:spring-test")
//    testImplementation("org.springframework.boot:spring-boot-starter-test") {
//        exclude(group = "org.mockito", module = "mockito-core")
//    }

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.sun.mail:jakarta.mail:2.0.2")
}
tasks.test { useJUnitPlatform() }