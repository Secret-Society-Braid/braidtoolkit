plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    alias(libs.plugins.moduleplugin)
    alias(libs.plugins.shadow)
    jacoco
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    google()
}

group = "org.braid.society.secret"

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.bundles.tests)
    testRuntimeOnly(libs.jupiter.engine)

}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn("test")
    reports {
        xml.required.set(false)
        html.required.set(true)
    }
}
