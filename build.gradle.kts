plugins {
    kotlin("jvm") version "1.8.21"
    `java-library`
}

repositories {
    mavenCentral()
}

java.toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
}

val arrowVersion = "1.2.0-RC"
dependencies {
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")
}

tasks.test {
    useJUnitPlatform()
}

val kotestVersion = "5.6.2"
val arrowKotestVersion = "1.3.3"
dependencies {
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:$arrowKotestVersion")
}