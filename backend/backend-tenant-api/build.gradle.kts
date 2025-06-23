plugins {
    id("java-library")
    id("io.spring.dependency-management") version "1.1.7"
}

dependencies {
    // Moduli interni
    implementation(project(":backend-core:backend-core-user"))
    implementation(project(":backend-shared"))

//    implementation("org.springframework.boot:spring-boot-starter-web")
//    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Dipendenze di test
    testImplementation("org.springframework:spring-test")
    // implementestImplementationtation("org.springframework.boot:spring-boot-starter-test")
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}