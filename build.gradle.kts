plugins {
    kotlin("jvm") version "1.9.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    `java-library`
}

repositories {
    mavenCentral()
}

java.toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
}

val arrowVersion = "1.2.0-RC"
dependencies {
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")
    implementation(kotlin("reflect"))
}

tasks.test {
    useJUnitPlatform()
}

val kotestVersion = "5.9.1"
val arrowKotestVersion = "1.4.0"
dependencies {
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:$arrowKotestVersion")
}
