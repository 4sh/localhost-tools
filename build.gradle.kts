plugins {
    base
    java
    kotlin("jvm")
    // see https://youtrack.jetbrains.com/issue/KTIJ-19369
    @Suppress("DSL_SCOPE_VIOLATION") alias(libs.plugins.versions)
    @Suppress("DSL_SCOPE_VIOLATION") alias(libs.plugins.version.catalog.update)
}

group = "sh.quatre"

val appVersion: Any = System.getenv("APP_VERSION") ?: "1.0.0-SNAPSHOT"
version = appVersion

allprojects {
    version = appVersion
    apply(plugin = "java")
    repositories {
        mavenCentral()
        google()
    }
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile>().all {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all", "-opt-in=kotlin.RequiresOptIn")
        }
    }
}

