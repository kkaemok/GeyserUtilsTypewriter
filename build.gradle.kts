plugins {
    kotlin("jvm") version "2.3.20"
    id("com.typewritermc.module-plugin") version "2.2.0"
    kotlin("plugin.lombok") version "2.3.20"
}

group = "me.kkaemok.typewriter"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.citizensnpcs.co/repo") {
        content { includeGroupAndSubgroups("net.citizensnpcs") }
    }
}

dependencies {
    compileOnly(files("libs/geyserutils-spigot-1.0-SNAPSHOT.jar"))
    compileOnly("org.geysermc.floodgate:api:2.2.5-SNAPSHOT")
    compileOnly("net.citizensnpcs:citizens-main:2.0.41-SNAPSHOT") {
        exclude(group = "*", module = "*")
    }
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

typewriter {
    namespace = "kkaemok"

    extension {
        name = "GeyserUtils"
        shortDescription = "Native Bedrock NPC dialogue integration through GeyserUtils."
        description = """
            Provides reusable Java and Bedrock platform branching plus native Bedrock
            NPC dialogue and option dialogue backed by the GeyserUtils Spigot API.
            Java dialogue remains configurable with an independent extension such as BetterHUD.
        """.trimIndent()
        engineVersion = "0.9.0-beta-177"
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA

        dependencies {
            dependency("typewritermc", "Entity")
        }

        paper {
            dependency("GeyserUtils")
            dependency("floodgate")
        }
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
