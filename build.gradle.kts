plugins {
    kotlin("jvm") version "1.7.10"
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
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test-junit5"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:${Version.JUNIT}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Version.JUNIT}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Version.JUNIT}")


    implementation("org.kotlinmath:complex-numbers:${Version.KOTLIN_MATH}")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        minHeapSize = "512m"
        maxHeapSize = "1024m"
    }
}