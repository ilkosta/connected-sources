plugins {
    id("java-library")
    id("io.spring.dependency-management") version "1.1.7"
}

//dependencyManagement {
//    imports {
//        mavenBom("org.springframework:spring-framework-bom:6.2.0") // Compatibile con Spring Boot 3.5.0
//    }
//}



dependencies {
    implementation(project(":backend-tenant-api"))
    implementation(project(":backend-tenant-fs-impl"))
    // Core di Spring (NO spring-boot!)
    implementation("org.springframework:spring-context")
    // https://mvnrepository.com/artifact/com.github.slugify/slugify
    implementation("com.github.slugify:slugify:3.0.7")

    implementation("org.springframework:spring-jdbc")
//    implementation("org.springframework.boot:spring-boot-starter")
//    implementation("org.springframework.boot:spring-boot-starter-web")
//    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Moduli interni
    implementation(project(":backend-shared"))

    // Dipendenze di test
    testImplementation("org.springframework:spring-test")
}


//java {
//    sourceCompatibility = JavaVersion.VERSION_21
//    targetCompatibility = JavaVersion.VERSION_21
//}
