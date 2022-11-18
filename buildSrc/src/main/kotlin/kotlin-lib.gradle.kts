import org.gradle.kotlin.dsl.*

plugins {
    base
    java
    kotlin("jvm")
}

kotlinProject()
withTestSupportProject()