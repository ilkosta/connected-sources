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
    // Core di Spring (NO spring-boot!)
    implementation("org.springframework:spring-context")

    // Moduli interni
    implementation(project(":backend-shared"))

    // Dipendenze di test
    testImplementation("org.springframework:spring-test")
}


//java {
//    sourceCompatibility = JavaVersion.VERSION_21
//    targetCompatibility = JavaVersion.VERSION_21
//}
