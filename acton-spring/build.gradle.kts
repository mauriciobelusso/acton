plugins {
    `java-library`
}

group = "dev.acton"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":acton-core"))
    implementation("org.springframework.boot:spring-boot-starter-web:3.5.6")
    compileOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
    compileOnly("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.13")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
}