import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dev.kikugie.loom-back-compat")
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("dev.deftu.gradle.bloom") version "0.2.0"
    id("me.modmuss50.mod-publish-plugin") version "1.1.0"
}

val oneConfigVersion = "1.0.0-beta.4"

val modid = property("mod.id") as String
val modname = property("mod.name") as String
val modversion = property("mod.version") as String
val mcversion = property("minecraft_version") as String
val versionrange = property("minecraft_version_range")
val loaderversion = property("loader_version")

base {
    archivesName.set("$modid-$modversion+$mcversion")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()

    maven("https://maven.parchmentmc.org")
    maven("https://repo.polyfrost.org/releases")
    maven("https://repo.polyfrost.org/snapshots")
    maven("https://maven.gegy.dev/releases")

    maven("https://central.sonatype.com/repository/maven-snapshots")
    maven("https://maven.logix.dev/snapshots") {
        content { excludeGroup("net.kyori") }
    }
    maven("https://nexus.prsm.wtf/repository/maven-public/maven-repo/releases/")
    maven("https://repo.hypixel.net/repository/Hypixel/")
    maven("https://maven.deftu.dev/releases")

    maven("https://maven.fabricmc.net/releases")
    maven("https://jitpack.io") {
        content { includeGroupAndSubgroups("com.github") }
    }
    maven("https://maven.bawnorton.com/releases") {
        content { includeGroup("com.github.bawnorton.mixinsquared") }
    }
    maven("https://maven.azureaaron.net/releases") {
        content { includeGroup("net.azureaaron") }
    }
    maven("https://redirector.kotlinlang.org/maven/compose-dev")
}

loom {
    runConfigs.all {
        ideConfigGenerated(stonecutter.current.isActive)
        runDir = "../../run"
    }

    runConfigs.remove(runConfigs["server"])
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")

    val hasOfficialMappings = findProperty("has_official_mappings")?.toString()?.toBoolean() ?: true
    if (hasOfficialMappings) {
        @Suppress("UnstableApiUsage")
        mappings(loom.layered {
            officialMojangMappings()
            optionalProp("${property("parchment_version")}") {
                parchment("org.parchmentmc.data:parchment-${property("minecraft_version")}:$it@zip")
            }
            optionalProp("${property("yalmm_version")}") {
                mappings("dev.lambdaurora:yalmm-mojbackward:${property("minecraft_version")}+build.$it")
            }
        })
    } else {
        findProperty("mappings_version")?.toString()?.takeUnless { it.isBlank() }?.let {
            mappings(it)
        }
    }

    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    modImplementation("org.polyfrost.oneconfig:${property("minecraft_version")}-fabric:$oneConfigVersion")
    implementation("org.polyfrost.oneconfig:commands:$oneConfigVersion")
    implementation("org.polyfrost.oneconfig:config:$oneConfigVersion")
    implementation("org.polyfrost.oneconfig:config-impl:$oneConfigVersion")
    implementation("org.polyfrost.oneconfig:events:$oneConfigVersion")
    implementation("org.polyfrost.oneconfig:internal:$oneConfigVersion")
    implementation("org.polyfrost.oneconfig:ui:$oneConfigVersion")
    implementation("org.polyfrost.oneconfig:utils:$oneConfigVersion")
    implementation("org.polyfrost.oneconfig:hud:$oneConfigVersion")
}

bloom {
    replacement("@MOD_ID@", modid)
    replacement("@MOD_NAME@", modname)
    replacement("@MOD_VERSION@", modversion)
}

tasks.processResources {
    val postEffectJson = when {
        mcversion == "1.21.4" -> """
            {
                "targets": {},
                "passes": [
                    {
                        "program": "polyblur:post/phosphor_motion_blur_legacy",
                        "inputs": [
                            { "sampler_name": "Diffuse", "target": "minecraft:main" },
                            { "sampler_name": "Prev", "target": "polyblur:previous" }
                        ],
                        "uniforms": [
                            { "name": "Strength", "values": [ 0.4 ] }
                        ],
                        "output": "minecraft:main"
                    }
                ]
            }
        """.trimIndent()
        mcversion == "1.21.5" -> """
            {
                "targets": {},
                "passes": [
                    {
                        "vertex_shader": "minecraft:post/blit",
                        "fragment_shader": "polyblur:post/phosphor_motion_blur_legacy",
                        "inputs": [
                            { "sampler_name": "Diffuse", "target": "minecraft:main" },
                            { "sampler_name": "Prev", "target": "polyblur:previous" }
                        ],
                        "uniforms": [
                            { "name": "Strength", "type": "float", "values": [ 0.4 ] }
                        ],
                        "output": "minecraft:main"
                    }
                ]
            }
        """.trimIndent()
        mcversion == "1.21.8" -> """
            {
                "targets": {},
                "passes": [
                    {
                        "vertex_shader": "minecraft:post/blit",
                        "fragment_shader": "polyblur:post/phosphor_motion_blur",
                        "inputs": [
                            { "sampler_name": "Diffuse", "target": "minecraft:main" },
                            { "sampler_name": "Prev", "target": "polyblur:previous" }
                        ],
                        "uniforms": {
                            "BlurConfig": [
                                {
                                    "name": "Strength",
                                    "type": "float",
                                    "value": 0.4
                                }
                            ]
                        },
                        "output": "minecraft:main"
                    }
                ]
            }
        """.trimIndent()
        else -> """
            {
                "targets": {},
                "passes": [
                    {
                        "vertex_shader": "minecraft:core/screenquad",
                        "fragment_shader": "polyblur:post/phosphor_motion_blur",
                        "inputs": [
                            { "sampler_name": "Diffuse", "target": "minecraft:main" },
                            { "sampler_name": "Prev", "target": "polyblur:previous" }
                        ],
                        "uniforms": {
                            "BlurConfig": [
                                {
                                    "name": "Strength",
                                    "type": "float",
                                    "value": 0.4
                                }
                            ]
                        },
                        "output": "minecraft:main"
                    }
                ]
            }
        """.trimIndent()
    }

    val props = mapOf(
        "mod_id" to modid,
        "mod_name" to modname,
        "mod_version" to modversion,
        "minecraft_version_range" to versionrange,
        "loader_version" to loaderversion,
        "java_version" to "JAVA_${findProperty("java_version")?.toString() ?: "21"}"
    )

    inputs.properties(props)
    inputs.property("postEffectJson", postEffectJson)

    filesMatching(listOf("fabric.mod.json", "mixins.$modid.json")) {
        expand(props)
    }

    exclude("assets/polyblur/post_effect/phosphor_motion_blur.json")

    if (mcversion != "1.21.1") {
        exclude(
            "assets/minecraft/shaders/post/phosphor_motion_blur.json",
            "assets/minecraft/shaders/program/phosphor_motion_blur.json",
            "assets/minecraft/shaders/program/phosphor_motion_blur.fsh"
        )
    }

    if (mcversion != "1.21.4") {
        exclude("assets/polyblur/shaders/post/phosphor_motion_blur_legacy.json")
    }

    doLast {
        val output = destinationDir.resolve("assets/polyblur/post_effect/phosphor_motion_blur.json")
        output.parentFile.mkdirs()
        output.writeText("$postEffectJson\n")
    }
}

val javaVersionStr = findProperty("java_version")?.toString() ?: "21"
val javaVersionInt = javaVersionStr.toInt()

val kotlinJvmTarget = when (javaVersionInt) {
    21 -> JvmTarget.JVM_21
    22 -> JvmTarget.JVM_22
    23 -> JvmTarget.JVM_23
    24 -> JvmTarget.JVM_24
    25 -> JvmTarget.JVM_25
    else -> JvmTarget.JVM_21
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaVersionInt)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(kotlinJvmTarget)
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersionInt))
    }
}

tasks.jar {
    inputs.property("archivesName", base.archivesName)

    from("LICENSE") {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}

fun <T> optionalProp(property: String, block: (String) -> T?): T? =
    findProperty(property)?.toString()?.takeUnless { it.isBlank() }?.let(block)

val modrinthMinecraftVersionOverride = mapOf(
    "1.21.8" to listOf("1.21.7", "1.21.8"),
    "1.21.10" to listOf("1.21.9", "1.21.10"),
    "26.1" to listOf("26.1", "26.1.1", "26.1.2")
)

val modrinthId = listOf("oneconfig.publish.modrinth", "publish.modrinth").firstNotNullOfOrNull { findProperty(it) }?.toString()?.takeIf { it.isNotBlank() }
val modrinthToken = listOf("oneconfig.publish.modrinth.token", "publish.modrinth.token", "modrinth.token").firstNotNullOfOrNull { findProperty(it) }?.toString()?.takeIf { it.isNotBlank() }
val minecraftVersion = modrinthMinecraftVersionOverride[mcversion] ?: listOf(mcversion)
val publishJarTaskName = if ("remapJar" in tasks.names) "remapJar" else "jar"
val changelogs = rootProject.file("CHANGELOG.md").takeIf { it.exists() }?.readText() ?: "No changelog provided."

val validateChangelog by tasks.registering {
    description = "Validates that the changelog is written for the current version."
    if (!changelogs.contains(modversion)) {
        throw GradleException("Changelog for version $modversion not found.")
    }
}

tasks.publishMods.configure {
    dependsOn(validateChangelog)
}
tasks.matching { it.name == "publishModrinth" }.configureEach {
    dependsOn(validateChangelog)
}

publishMods {
    file = tasks.named<AbstractArchiveTask>(publishJarTaskName).flatMap { it.archiveFile }

    displayName = modversion
    version = "v$modversion"
    changelog = changelogs
    type = BETA

    modLoaders.add("fabric")

    dryRun = modrinthId == null || modrinthToken == null

    if (modrinthId != null) {
        modrinth {
            projectId = modrinthId
            accessToken = modrinthToken.orEmpty()

            minecraftVersions.addAll(minecraftVersion)

            requires("oneconfig")
            requires("fabric-language-kotlin")
        }
    }
}
