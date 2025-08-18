plugins {
    id("java-library")
//    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    // Moduli interni
//    implementation(project(":backend-core:backend-core-user"))
    implementation(project(":backend-shared"))

    implementation("org.springframework.boot:spring-boot-starter")
//    implementation("org.springframework.boot:spring-boot-starter-web")
//    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Dipendenze di test
    testImplementation("org.springframework:spring-test")
    // implementestImplementationtation("org.springframework.boot:spring-boot-starter-test")
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-test
    // Dipendenze di test
    testImplementation("org.springframework.boot:spring-boot-starter-test")


}
tasks.test { useJUnitPlatform() }