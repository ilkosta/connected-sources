plugins {
    id("java-library")
    id("org.springframework.boot") version "3.5.0"
//    id("io.spring.dependency-management") version "1.1.7"
}

val flywayVersion = "11.13.0" // Ultima versione stabile


dependencies {
    // Moduli interni
    implementation(project(":backend-core:backend-core-user"))
    implementation(project(":backend-tenant-api"))
    implementation(project(":backend-shared"))
    implementation(project(":backend-notification"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    /// auth with jwt...
    implementation("org.springframework.security:spring-security-core")
    // Basic JWT support (optional, for Claims interface)
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
    /// ----

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
    implementation("org.hibernate.validator:hibernate-validator:9.0.1.Final")

//    implementation("org.flywaydb:flyway-core")
//    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("net.logstash.logback:logstash-logback-encoder:8.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")

    // https://mvnrepository.com/artifact/org.jetbrains/annotations
    implementation("org.jetbrains:annotations:26.0.2")
    // per permettere la generazione del META-INF/spring-configuration-metadata.json
    // durante la compilazione, in modo da facilitare l'editazione degli yaml
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // per risolvere problemi di compatibilità tra versioni di java nei test...
    implementation("org.graalvm.sdk:graal-sdk:25.0.0")
    implementation("org.graalvm.regex:regex:25.0.0")
    implementation("org.graalvm.js:js:25.0.0")

    // da github:
//    Karate Core "Fat JAR"
//    If you mix Karate into a Maven or Gradle project with many other dependendies, you may run into problems because of dependency conflicts. For example a lot of Java projects directly (or indirectly) depend on Netty or Thymeleaf or ANTLR, etc.
//
//    If you face issues such as "class not found", just pull in the karate-core dependency, and use the all classifier in your pom.xml (or build.gradle).
    testImplementation("io.karatelabs:karate-core:1.5.1:all")
//    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Dipendenze di test
    testImplementation(project(":backend-tenant-fs-impl")) // solo per test
    testImplementation(project(":backend-tenant-db-impl")) // solo per test
    testImplementation("org.springframework:spring-test")
    testImplementation("org.springframework.security:spring-security-test")

    testImplementation("org.flywaydb:flyway-core")
    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.intuit.karate:karate-junit5:1.4.1")
    testImplementation("net.bytebuddy:byte-buddy-agent:1.17.7")

    tasks.test {
        // Point to the agent on the classpath
        doFirst {
            val agent = configurations.testRuntimeClasspath.get().files
                .first { it.name.startsWith("byte-buddy-agent") && it.extension == "jar" }
            jvmArgs("-javaagent:${agent.absolutePath}")
        }
        // Optional (hides extra warnings on newer JDKs)
        jvmArgs("-XX:+EnableDynamicAgentLoading")


        useJUnitPlatform {
            excludeTags("karate")
        }
    }
}

tasks.bootRun {
    systemProperty("spring.profiles.active", "dev")
//    systemProperty("server.port", "8081")
    // Add other system properties as needed
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("org.connected_sources.api.BackendApiApplication")
}

tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    mainClass.set("org.connected_sources.api.BackendApiApplication")
}
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

tasks.register("showPostgresVersion") {
    doLast {
        configurations.runtimeClasspath.get()
            .filter { it.name.contains("postgres", ignoreCase = true) }
            .forEach { file ->
                println("PostgreSQL JDBC: ${file.name}")
                println("Path: ${file.absolutePath}")
            }
    }
}

tasks.register("analyzeLocalDependencies") {
    group = "help"
    description = "Analisi dettagliata delle dipendenze tra moduli locali"

    doLast {
        val dependencyMap = mutableMapOf<String, MutableList<String>>()

        rootProject.allprojects.forEach { module ->
            val dependencies = mutableListOf<String>()

            listOf("implementation", "api", "compileOnly").forEach { configName ->
                val config = module.configurations.findByName(configName)
                if (config != null) {
                    config.dependencies
                        .filterIsInstance<ProjectDependency>()
                        .forEach {
                            dependencies.add("${it.name} (${configName})")
                        }
                }
            }

            if (dependencies.isNotEmpty()) {
                dependencyMap[module.path] = dependencies
            }
        }

        println("=== ANALISI DIPENDENZE LOCALI ===")
        println("Moduli trovati: ${dependencyMap.size}")
        println()

        dependencyMap.forEach { (module, deps) ->
            println("🔍 $module")
            deps.forEach { dep ->
                println("   ➤ $dep")
            }
            println()
        }

        val totalDeps = dependencyMap.values.sumOf { it.size }
        println("📊 Statistiche:")
        println("   - Dipendenze totali: $totalDeps")
        println("   - Moduli con dipendenze: ${dependencyMap.size}")
    }
}
//
//tasks.register("localDependencyGraph") {
//    group = "help"
//    description = "Grafico completo delle dipendenze locali con gestione errori"
//
//    doLast {
//        println("=== GRAFICO DIPENDENZE LOCALI ===")
//
//        rootProject.allprojects
//            .filter { it != rootProject }
//            .sortedBy { it.path }
//            .forEach { module ->
//                println("\n🏗️  ${module.path}")
//
//                val configs = listOf("implementation", "api", "compileOnly", "runtimeOnly")
//
//                configs.forEach { configName ->
//                    val config = module.configurations.findByName(configName)
//                    val deps = config?.dependencies
//                        ?.filterIsInstance<ProjectDependency>()
//                        ?.map { it.name }
//                        ?.sorted()
//                        ?: emptyList()
//
//                    if (deps.isNotEmpty()) {
//                        println("   ⚙️  $configName:")
//                        deps.forEach { dep ->
//                            println("      → $dep")
//                        }
//                    }
//                }
//            }
//    }
//}

// Forza l'uso delle versioni specificate
configurations.all {
    resolutionStrategy {
        force(
            "org.flywaydb:flyway-core:$flywayVersion",
            "org.flywaydb:flyway-database-postgresql:$flywayVersion"
        )
    }
}