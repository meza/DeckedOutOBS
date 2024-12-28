import java.util.*

plugins {
    id("dev.architectury.loom")
    id("me.modmuss50.mod-publish-plugin")
}

fun String.upperCaseFirst() = replaceFirstChar { if (it.isLowerCase()) it.uppercaseChar() else it }

val minecraftVersion = stonecutter.current.version
val customPropsFile = rootProject.file("versions/dependencies/${minecraftVersion}.properties")

if (customPropsFile.exists()) {
    val customProps = Properties().apply {
        customPropsFile.inputStream().use { load(it) }
    }
    customProps.forEach { key, value ->
        project.extra[key.toString()] = value
    }
}

class ModData {
    val id: String get() = property("mod.id").toString()
    val name: String get() = property("mod.name").toString()
    val description: String get() = property("mod.description").toString()
    val version: String get() = property("mod.version").toString()
    val group: String get() = property("mod.group").toString()
    fun prop(key: String) = project.extra[key].toString()
}

val mod = ModData()
val loader = project.name.substringAfterLast("-").lowercase()
val isFabric = loader == "fabric"
val isForge = loader == "forge"
val isNeoforge = loader == "neoforge"
val isForgeLike = isNeoforge || isForge

version = "${mod.version}+mc${minecraftVersion}"
group = mod.group

val isBeta = "next" in version.toString()


base { archivesName.set("${mod.id}-${loader}") }

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.architectury.dev")
    maven("https://maven.minecraftforge.net")
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.shedaniel.me/")
    maven("https://api.modrinth.com/maven")
}

loom {
    accessWidenerPath = rootProject.file("src/main/resources/${mod.id}.accesswidener")
    runConfigs.all {
        isIdeConfigGenerated = true
        runDir = "../../run"
        vmArgs("-Dmixin.debug.export=true")
        if (environment == "client") programArgs("--username=houseofmeza")
    }

    decompilers {
        get("vineflower").apply { options.put("mark-corresponding-synthetics", "1") }
    }
}

configurations.configureEach {
    resolutionStrategy.force("net.sf.jopt-simple:jopt-simple:5.0.4")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:${mod.prop("yarn_mappings")}:v2")

    modImplementation("net.fabricmc:fabric-loader:${mod.prop("loader_version")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${mod.prop("fabric_version")}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5+")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5+")
}

val buildAndCollect = tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.remapJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs"))
    dependsOn("build")
}

if (stonecutter.current.isActive) {
    rootProject.tasks.register("buildActive") {
        group = "project"
        dependsOn(buildAndCollect)
    }
    rootProject.tasks.register("runActive") {
        group = "project"
        dependsOn(tasks.named("runClient"))
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val map = mapOf(
        "id" to mod.id,
        "name" to mod.name,
        "description" to mod.description,
        "version" to mod.version,
        "minecraftVersion" to minecraftVersion
    )
    filesMatching("fabric.mod.json") { expand(map) }
}

stonecutter {
    const("fabric", loader == "fabric")
    const("forge", loader == "forge")
    const("neoforge", loader == "neoforge")
    val j21 = eval(minecraftVersion, ">=1.20.6")
    java {
        withSourcesJar()
        sourceCompatibility = if (j21) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
        targetCompatibility = if (j21) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    }
}

tasks.register("configureMinecraft") {
    group = "project"
    val runDir = "${rootProject.projectDir}/run"
    val optionsFile = file("$runDir/options.txt")

    doFirst {
        println("Configuring Minecraft options...")
        optionsFile.parentFile.mkdirs()
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

tasks.named("runClient") {
    dependsOn(tasks.named("configureMinecraft"))
}

publishMods {
    changelog.set(
        rootProject.file("CHANGELOG.md").takeIf { it.exists() }?.readText() ?: "No changelog provided."
    )
    file = tasks.remapJar.get().archiveFile
    version = "${mod.version}+${loader}-${minecraftVersion}"
    type.set(if (isBeta) BETA else STABLE)
    modLoaders.add(loader)
    displayName = "${mod.version} for ${loader.upperCaseFirst()} $minecraftVersion"

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN").orElse("")
        projectId = providers.environmentVariable("MODRINTH_ID").orElse("0")
        minecraftVersions.add(stonecutter.current.version)
        announcementTitle = "Download ${mod.version}+${loader}-${minecraftVersion}from Modrinth"
        requires("fabric-api")
    }


    dryRun = providers.environmentVariable("DO_PUBLISH").getOrElse("true").toBoolean()
}
