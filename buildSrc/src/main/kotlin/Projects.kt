import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.the

val Project.libs get() = the<org.gradle.accessors.dm.LibrariesForLibs>()

fun Project.kotlinProject() {
    dependencies {
        "implementation"(kotlin("stdlib-jdk8"))
        "implementation"(libs.kotlinx.datetime)

        "implementation"(libs.log4j.api)
    }
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

fun Project.withTestSupportProject() {
    dependencies {
        "testImplementation"(kotlin("test-junit5"))
        "testImplementation"(libs.junit.api)
        "testImplementation"(libs.junit.params)
        "testRuntimeOnly"(libs.junit.engine)
        "testImplementation"(libs.strikt.core)
        "testImplementation"(libs.mockk)
        "testImplementation"(libs.log4j.core)
    }
    tasks.getByPath("test").doFirst {
        with(this as Test) {
            useJUnitPlatform()
        }
    }
}

fun Project.dockerRepository(): String {
    val repositoryUrlPropertyName = "dockerRepositoryUrl"
    return if (project.hasProperty(repositoryUrlPropertyName)) {
        project.property(repositoryUrlPropertyName) as String
    } else "europe-docker.pkg.dev/quatreapp/localhost-tools"
}
