pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.meza.gg/snapshots")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.terraformersmc.com/")
        maven("https://maven.shedaniel.me/")
    }
}

plugins {
    id("gg.meza.stonecraft") version "1.12.3"
    id("dev.kikugie.stonecutter") version "0.9.6"
}

stonecutter {
    shared {
        fun mc(version: String, vararg loaders: String) {
            for (it in loaders) version("$version-$it", version)
        }

        mc("26.1", "fabric")
        mc("26.2", "fabric")

        vcsVersion = "26.2-fabric"
    }
    create(rootProject)
}

rootProject.name = "DeckedOutOBS"
