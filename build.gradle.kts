val groupId = "dev.acton"
val actonVersion = providers.gradleProperty("actonVersion").get()

allprojects {
    group = groupId
    version = actonVersion
}

subprojects {
    pluginManager.withPlugin("java") {
        the<JavaPluginExtension>().toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        tasks.withType<Test>().configureEach { useJUnitPlatform() }
    }
    pluginManager.withPlugin("java-library") {
        the<JavaPluginExtension>().toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        tasks.withType<Test>().configureEach { useJUnitPlatform() }
    }
}

tasks.register("publishAllToMavenLocal") {
    subprojects.forEach { p ->
        p.plugins.withId("maven-publish") {
            dependsOn(p.tasks.named("publishToMavenLocal"))
        }
    }
}

tasks.register("buildAllAndPublishLocal") {
    group = "publishing"
    description = "Builds and publishes all ActOn modules to the local Maven repository"

    dependsOn(subprojects.mapNotNull { it.tasks.findByName("build") })

    subprojects.forEach { p ->
        p.plugins.withId("maven-publish") {
            dependsOn(p.tasks.named("publishToMavenLocal"))
        }
    }
}
