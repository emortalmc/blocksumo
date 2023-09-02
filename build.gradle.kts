plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    implementation("dev.emortal:rayfast:e6ebf1f")
    implementation("com.github.emortaldev:Particable:f7212f39fb")

    implementation("dev.emortal.minestom:game-sdk:de763e2") {
        exclude("dev.emortal.minestom", "core")
    }
    implementation("dev.emortal.minestom:core:209bab1")

    implementation("net.kyori:adventure-text-minimessage:4.14.0")
    compileOnly("org.jetbrains:annotations:24.0.1")

    implementation("dev.hollowcube:polar:1.3.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
    }
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        mergeServiceFiles()

        manifest {
            attributes (
                "Main-Class" to "dev.emortal.minestom.blocksumo.Main",
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
