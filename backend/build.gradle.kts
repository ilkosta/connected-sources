plugins {
    java
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
}

allprojects {
    group = "org.connected-sources"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

//java {
//    toolchain {
//        languageVersion = JavaLanguageVersion.of(21)
//    }
//}
//
//configurations {
//    compileOnly {
//        extendsFrom(configurations.annotationProcessor.get())
//    }
//}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java-library")
//    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    repositories {
        mavenCentral()
    }

//    java {
//        sourceCompatibility = JavaVersion.VERSION_21
//        targetCompatibility = JavaVersion.VERSION_21
//    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

//    configurations {
//        compileOnly {
//            extendsFrom(configurations.annotationProcessor.get())
//        }
//    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework:spring-framework-bom:6.2.0")
        }
    }

    dependencies {
        // Test con JUnit 5 e Mockito

        testImplementation("org.mockito:mockito-junit-jupiter:5.18.0")
        testImplementation("org.mockito:mockito-core:5.18.0")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.1")
        testImplementation("org.springframework:spring-test")

        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.1")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    }

    tasks.withType<Test> {
        useJUnitPlatform()
        // Aggiungi logging per debug
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}
