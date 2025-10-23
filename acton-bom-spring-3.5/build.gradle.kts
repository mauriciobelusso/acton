plugins {
    `java-platform`
    `maven-publish`
}

val actonVersion = project.version.toString()
val bootVersion = providers.gradleProperty("bootVersion").get()

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:$bootVersion"))
    constraints {
        api("dev.acton:acton-core:$actonVersion")
        api("dev.acton:acton-spring:$actonVersion")
        api("dev.acton:acton-spring-store:$actonVersion")
        api("dev.acton:acton-spring-openapi:$actonVersion")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
            artifactId = "acton-bom-spring-3.5"
        }
    }
}
