import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    id("org.springframework.boot") version "3.5.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("java") // or java-library if needed globally
}

allprojects {
    group = "org.connected-sources"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}


repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")

    repositories {
        mavenCentral()
        maven { url = uri("https://repo.spring.io/release") }
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(24)
        }
    }

//    configurations {
//        compileOnly {
//            extendsFrom(configurations.annotationProcessor.get())
//        }
//    }

//    // con pluginManagement
//    dependencyManagement {
//        imports {
////            mavenBom("org.springframework:spring-framework-bom:6.2.0")
//            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.0")
//        }
//    }

    // Kotlin DSL form of `dependencyManagement { ... }`
    extensions.configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.0")
        }
    }

    dependencies {
        // Test con JUnit 5 e Mockito
        testImplementation("org.mockito:mockito-junit-jupiter:5.18.0")
        testImplementation("org.mockito:mockito-core:5.18.0")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.1")
        testImplementation("org.springframework:spring-test")

        testImplementation(platform("org.junit:junit-bom:5.13.1"))
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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
