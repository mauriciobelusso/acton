plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

group = "dev.acton"
version = "0.1.0-SNAPSHOT"
description = "ActOn Framework — Declarative Architecture for Java"

subprojects {
    repositories {
        mavenCentral()
    }
}
