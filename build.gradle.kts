@file:Suppress("UnstableApiUsage")
plugins {
    id("fabric-loom")
}
val minecraftVersion: String = stonecutter.current.version

version = "${mod.version}+mc${minecraftVersion}"

base {
    archivesName.set(mod.id)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:${mod.prop("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${mod.prop("loader_version")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${mod.prop("fabric_version")}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5+")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5+")
}

loom {
//    accessWidenerPath = rootProject.file("src/main/resources/${mod.id}.accesswidener")

    decompilers {
        get("vineflower").apply { // Adds names to lambdas - useful for mixins
            options.put("mark-corresponding-synthetics", "1")
        }
    }

    runConfigs.all {
        isIdeConfigGenerated = true
        runDir = "../../run"
        vmArgs("-Dmixin.debug.export=true")
    }
}

java {
    val java = if (stonecutter.eval(minecraftVersion, ">=1.20.5"))
        JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = java
    sourceCompatibility = java
}

tasks.processResources {
    properties(listOf("fabric.mod.json"),
        "id" to project.mod.id,
        "name" to project.mod.name,
        "description" to project.mod.description,
        "version" to project.mod.version,
        "minecraftVersion" to minecraftVersion
    )
}


tasks.register("configureMinecraft") {
    group = "project"
    val runDir = "${rootProject.projectDir}/run"
    val optionsFile = file("$runDir/options.txt")

    doFirst {

        println("Configuring Minecraft options...")

        // Ensure the run directory exi sts
        optionsFile.parentFile.mkdirs()

        // Write the desired options to 'options.txt'
        optionsFile.writeText(
            """
            guiScale:3
            fov=90
            narrator:0
            soundCategory_music:0.0
            darkMojangStudiosBackground:true
            """.trimIndent()
        )
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
    from(tasks.remapJar.get().archiveFile, tasks.remapSourcesJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs"))
    dependsOn("build")
}

tasks.named("runClient") {
    dependsOn("configureMinecraft")
}
