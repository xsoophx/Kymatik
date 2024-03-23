plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0-Beta3"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

group = "cc.suffro"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    repositories {
        mavenCentral()
    }

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("stdlib-jdk8"))
        testImplementation(kotlin("test-junit5"))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
