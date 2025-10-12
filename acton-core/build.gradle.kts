plugins {
    `java-library`
    `maven-publish`
}

group = "dev.acton"
version = "0.1.0-SNAPSHOT"
description = "ActOn Core — Foundation of the ActOn Framework"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("ActOn Core")
                description.set("The foundation of the ActOn Framework — contracts, actors, stores.")
                url.set("https://github.com/mauriciobelusso/acton")
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
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
