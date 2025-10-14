plugins { `java-library` }

group = "dev.acton"
version = "0.1.0-SNAPSHOT"
description = "ActOn OpenAPI Spring — Springdoc adapter (no own endpoint/UI)"

repositories { mavenCentral() }

dependencies {
    api(project(":acton-spring"))

    // Spring Boot autoconfig
    implementation("org.springframework.boot:spring-boot-autoconfigure:3.5.6")

    compileOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
    compileOnly("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.13")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test { useJUnitPlatform() }
