pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "acton"

include(
    "samples:acton-sample-spring",
    "acton-bom",
    "acton-bom-spring-3.5",
    "acton-core",
    "acton-spring",
    "acton-spring-store",
    "acton-spring-openapi"
)
