plugins { `java-library` }

group = "dev.acton"
version = "0.1.0-SNAPSHOT"
description = "ActOn OpenAPI — generator from ActOn Router routes"

repositories { mavenCentral() }

dependencies {
    api(project(":acton-router"))

    implementation("io.swagger.core.v3:swagger-core-jakarta:2.2.38")
    implementation("io.swagger.core.v3:swagger-models-jakarta:2.2.38")
    implementation("tools.jackson.core:jackson-databind:3.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
tasks.test { useJUnitPlatform() }
