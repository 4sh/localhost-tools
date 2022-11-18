plugins {
    base
    application
    kotlin("jvm")
    `kotlin-lib`
}

dependencies {
    implementation(project(":localhost-dev-proxy:dev-proxy-lib"))
    implementation(libs.log4j.core)
    implementation(libs.http4k.core)
    implementation(libs.http4k.server.undertow)
}