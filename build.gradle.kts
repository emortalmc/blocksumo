plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.emortal.minestom.blocksumo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://repo.emortal.dev/snapshots")
    maven("https://repo.emortal.dev/releases")

    maven("https://jitpack.io")
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation("dev.emortal.minestom:game-sdk:08edba4") {
        exclude(group = "dev.emortal.api", module = "common-proto-sdk")
    }
    implementation("dev.emortal.api:common-proto-sdk:91d6f2e")

    implementation("dev.emortal:rayfast:e6ebf1f")
    implementation("com.github.emortaldev:Particable:f7212f39fb")

    implementation("net.kyori:adventure-text-minimessage:4.14.0")
    compileOnly("org.jetbrains:annotations:24.0.1")

    implementation("dev.hollowcube:polar:1.3.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    shadowJar {
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

    build {
        dependsOn(shadowJar)
    }
}
