plugins {
    `java-library`
    `maven-publish`
}

val bootVersion = providers.gradleProperty("bootVersion").get()

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:$bootVersion"))
    api(project(":acton-core"))
    api("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework:spring-orm")
    compileOnly("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:$bootVersion"))
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.springframework:spring-test")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "acton-spring-store"
        }
    }
}
