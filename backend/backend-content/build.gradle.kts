plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":backend-shared"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.6.0.202305301015-r")
    implementation("org.yaml:snakeyaml:2.0")
    runtimeOnly("org.postgresql:postgresql")
}