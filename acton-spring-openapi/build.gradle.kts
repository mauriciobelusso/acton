plugins {
    `java-library`
    `maven-publish`
}

val bootVersion = providers.gradleProperty("bootVersion").get()
val springdocVersion = providers.gradleProperty("springdocVersion").get()

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:$bootVersion"))
    api(project(":acton-spring"))
    compileOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "acton-spring-openapi"
        }
    }
}
