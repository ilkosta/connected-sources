plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":backend-content"))
    implementation(project(":backend-user"))
    implementation(project(":backend-tenant"))
    implementation(project(":backend-notification"))
    implementation(project(":backend-infra"))
    implementation(project(":backend-shared"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
}