plugins {
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    java
}

group = "dev.acton.sample"
version = "0.1.0-SNAPSHOT"

dependencies {
    implementation(platform("dev.acton:acton-bom-spring-3.5:0.1.0"))
    implementation(project(":acton-core"))
    implementation(project(":acton-spring"))
    implementation(project(":acton-spring-store"))
    implementation(project(":acton-spring-openapi"))
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}
