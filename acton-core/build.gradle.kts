plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    compileOnly("jakarta.persistence:jakarta.persistence-api:3.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "acton-core"
        }
    }
}
