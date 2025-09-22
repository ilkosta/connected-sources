plugins {
    id("java-library")
//    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    // Moduli interni
//    implementation(project(":backend-core:backend-core-user"))
    implementation(project(":backend-shared"))
    runtimeOnly(project(":backend-tenant-fs-impl")) // <== tante rogne in meno
    runtimeOnly(project(":backend-tenant-db-impl"))


    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Dipendenze di test
    testImplementation("org.springframework.boot:spring-boot-starter-test")


}
tasks.test { useJUnitPlatform() }