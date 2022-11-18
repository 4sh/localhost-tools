buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

// to make version catalog accessible in precompiled build scripts
// see https://github.com/gradle/gradle/issues/15383
dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "17"
}

dependencies {
    implementation(libs.plugin.kotlin.core)
}