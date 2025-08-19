plugins {
    id("java-library")
//    id("org.springframework.boot")
    id("io.spring.dependency-management") version "1.1.7"
}

//dependencyManagement {
//    imports {
//        mavenBom("org.springframework:spring-framework-bom:6.2.0") // Compatibile con Spring Boot 3.5.0
//    }
//}



dependencies {
    implementation(project(":backend-shared"))
    implementation(project(":backend-tenant-api"))
    implementation(project(":backend-tenant-fs-impl"))



    // Core di Spring (NO spring-boot!)
    implementation("org.springframework:spring-context")
    // https://mvnrepository.com/artifact/com.github.slugify/slugify
    implementation("com.github.slugify:slugify:3.0.7")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql") // sboot gestirà la versione

    implementation("org.springframework.boot:spring-boot-starter")
//    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")

    // Moduli interni
    implementation(project(":backend-shared"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
tasks.test { useJUnitPlatform() }

//java {
//    sourceCompatibility = JavaVersion.VERSION_21
//    targetCompatibility = JavaVersion.VERSION_21
//}
