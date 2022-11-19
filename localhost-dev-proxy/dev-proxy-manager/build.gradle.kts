plugins {
    base
    application
    kotlin("jvm")
    `kotlin-lib`
    id("com.palantir.docker")
}

dependencies {
    implementation(libs.log4j.core)
    implementation(libs.http4k.core)
    implementation(libs.http4k.server.undertow)
}

val archiveBaseName = "localhost-server-manager"
val jarFileName = "${project.name}-$version.jar"

tasks.getByName<Zip>("distZip").enabled = false
tasks.getByName<Tar>("distTar").exclude("**/$jarFileName")

application {
    mainClass.set("sh.quatre.localhost.dev.proxy.server.LocalDevProxyServerKt")
    applicationName = archiveBaseName
}

docker {
    name = "${dockerRepository()}/$archiveBaseName:$version"
    setDockerfile(File("Dockerfile"))
    buildArgs(
        mapOf(
            "APPLICATION_ARCHIVE_FILE_NAME" to "${archiveBaseName}-${version}.tar",
            "APPLICATION_JAR_FILE_NAME" to jarFileName,
            "APPLICATION_FOLDER_NAME" to "${archiveBaseName}-${version}",
        )
    )
    files(tasks.distTar.get().outputs.files)
    files(tasks.jar.get().outputs.files)
}
