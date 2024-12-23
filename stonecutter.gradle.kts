plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.9-SNAPSHOT" apply false
}
stonecutter active "1.21" /* [SC] DO NOT EDIT */
stonecutter.automaticPlatformConstants = true

// Builds every version into `build/libs/{mod.version}/`
stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("buildAndCollect")
}
