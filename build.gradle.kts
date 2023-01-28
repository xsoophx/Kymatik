plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

object Version {
    const val ASSERTK = "0.25"
    const val JUNIT = "5.9.1"
    const val KOTLIN_MATH = "1.0"
    const val LOGBACK = "1.2.3"
    const val LOGGING = "3.0.2"
    const val SLF4J = "2.0.3"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test-junit5"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:${Version.JUNIT}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Version.JUNIT}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Version.JUNIT}")

    implementation("org.kotlinmath:complex-numbers:${Version.KOTLIN_MATH}")

    implementation("io.github.microutils:kotlin-logging-jvm:${Version.LOGGING}")
    runtimeOnly("ch.qos.logback:logback-classic:${Version.LOGBACK}")
    implementation("org.slf4j:slf4j-simple:${Version.SLF4J}")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        minHeapSize = "512m"
        maxHeapSize = "1024m"
    }
}
