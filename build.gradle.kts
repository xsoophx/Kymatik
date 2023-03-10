plugins {
    application
    kotlin("jvm") version "1.8.10"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "cc.suffro"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.jetbrains.space/data2viz/p/maven/public")
    }
}

javafx {
    version = "19"
    modules = listOf("javafx.controls", "javafx.graphics", "javafx.media")
}

application {
    mainClass.set("cc.suffro.bpmanalyzer.Main")
}

object Version {
    const val ASSERTK = "0.25"
    const val COROUTINES = "1.7.0-Beta"
    const val JUNIT = "5.9.1"
    const val KOTLIN_MATH = "1.0"
    const val LOGBACK = "1.2.3"
    const val LOGGING = "3.0.2"
    const val SLF4J = "2.0.3"
    const val VIZ = "0.9.1"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test-junit5"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.COROUTINES}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:${Version.COROUTINES}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${Version.JUNIT}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Version.JUNIT}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Version.JUNIT}")

    implementation("org.kotlinmath:complex-numbers:${Version.KOTLIN_MATH}")

    implementation("io.github.microutils:kotlin-logging-jvm:${Version.LOGGING}")
    runtimeOnly("ch.qos.logback:logback-classic:${Version.LOGBACK}")
    implementation("org.slf4j:slf4j-simple:${Version.SLF4J}")

    implementation("io.data2viz.d2v:d2v-axis:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-color:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-delaunay:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-dsv:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-ease:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-force:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-format:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-scale:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-shape:${Version.VIZ}")
    implementation("io.data2viz.d2v:d2v-viz:${Version.VIZ}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        minHeapSize = "512m"
        maxHeapSize = "1024m"
    }
}
