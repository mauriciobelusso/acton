plugins {
    `java-library`
    `maven-publish`
}

group = "dev.acton"
version = "0.1.0-SNAPSHOT"
description = "ActOn Router — Contract discovery and actor binding"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    api(project(":acton-core"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("ActOn Router")
                description.set("Maps @Contract to Actor.on(...) and executes handlers.")
                url.set("https://github.com/mauriciobelusso/acton")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("mauriciobelusso")
                        name.set("Mauricio Belusso")
                    }
                }
            }
        }
    }
}
