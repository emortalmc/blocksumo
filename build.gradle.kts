plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.emortal.minestom.blocksumo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://repo.emortal.dev/snapshots")
    maven("https://repo.emortal.dev/releases")

    maven("https://jitpack.io")
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation("dev.hollowcube:polar:1.2.0")
    implementation("com.github.EmortalMC:Rayfast:9e5accb")
    implementation("com.github.emortaldev:Particable:f7212f39fb")

    implementation("dev.emortal.minestom:core:09311b0")
    implementation("net.kyori:adventure-text-minimessage:4.12.0")

    implementation("dev.emortal.minestom:game-sdk:4d22719")

    compileOnly("org.jetbrains:annotations:24.0.1")
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        mergeServiceFiles()

        manifest {
            attributes (
                "Main-Class" to "dev.emortal.minestom.blocksumo.Entrypoint",
                "Multi-Release" to true
            )
        }
    }

    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    build { dependsOn(shadowJar) }
}
