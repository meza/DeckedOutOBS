plugins {
    id("gg.meza.stonecraft")
}

modSettings {
    clientOptions {
        darkBackground = true
        musicVolume = 0.0
        narrator = false
    }
}


repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
