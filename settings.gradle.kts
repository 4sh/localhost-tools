// see https://docs.gradle.org/7.5/userguide/configuration_cache.html#config_cache:stable
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "localhost-tools"

include(
    "localhost-dev-proxy:dev-proxy-manager"
)