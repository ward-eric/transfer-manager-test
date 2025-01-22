java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

plugins {
    java
    application
    idea
    checkstyle

    id("com.github.spotbugs") version "6.0.19"
}

group = "com.test"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    implementation("software.amazon.awssdk:s3-transfer-manager:2.30.2")
    implementation("software.amazon.awssdk.crt:aws-crt:0.33.9")
}

application {
    mainClass = "com.test.TransferManagerTest"
    applicationDefaultJvmArgs = listOf("-XshowSettings:vm",
        "-XX:MinRAMPercentage=80.0",
        "-XX:MaxRAMPercentage=80.0")
}
