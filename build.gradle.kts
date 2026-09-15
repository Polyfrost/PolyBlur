import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dev.kikugie.loom-back-compat")
    id("org.jetbrains.kotlin.jvm") version "2.4.10"
    id("dev.deftu.gradle.bloom") version "0.2.0"
    id("me.modmuss50.mod-publish-plugin") version "2.2.0"
}

val modid: String = sc.properties["mod.id"]
val modname: String = sc.properties["mod.name"]
val modversion: String = sc.properties["mod.version"]
val mcversion: String = sc.current.version
val mcDependencyVersion: String = sc.properties.getOrNull<String>("deps.minecraft") ?: mcversion
val versionrange: String = sc.properties["mod.mc_compat"]
val loaderversion: String = sc.properties["deps.fabric_loader"]
val oneconfigversion: String = sc.properties["deps.oneconfig"]
val fapiversion: String = sc.properties["deps.fabric_api"]

version = "$modversion+$mcversion"
base.archivesName = modid

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

val compatibleVersions: List<String> = sc.properties.rawOrNull("mod", "mc_releases")
    ?.asList().orEmpty().map { it.toString() }

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }

    mavenLocal()
    mavenCentral()
    google()
    maven("https://repo.polyfrost.org/releases") { name = "Polyfrost Releases" }
    maven("https://repo.polyfrost.org/snapshots") { name = "Polyfrost Snapshots" }
    maven("https://central.sonatype.com/repository/maven-snapshots") {
        name = "Sonatype Snapshots"
        content { includeGroup("net.kyori") }
    }
    strictMaven("https://maven.deftu.dev/releases", "Deftu", "dev.deftu")
    strictMaven("https://maven.terraformersmc.com/", "TerraformersMC", "com.terraformersmc")
    strictMaven("https://maven.fabricmc.net/", "FabricMC", "net.fabricmc")
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcDependencyVersion")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:$loaderversion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fapiversion")
    modImplementation("org.polyfrost.oneconfig:$mcversion-fabric:$oneconfigversion")
    for (module in arrayOf("commands", "config", "config-impl", "events", "internal", "ui", "utils", "hud")) {
        implementation("org.polyfrost.oneconfig:$module:$oneconfigversion")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:${sc.properties.get<String>("deps.junit")}")
    testImplementation("net.fabricmc:fabric-loader-junit:$loaderversion")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1")
    }

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run")
        jvmArguments.add("-Dmixin.debug.export=true")

        if (project.hasProperty("autoWorld")) {
            programArgs("--quickPlaySingleplayer", project.property("autoWorld").toString())
        }
    }

    runConfigs.remove(runConfigs["server"])
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

val kotlinJvmTarget = JvmTarget.fromTarget(requiredJava.majorVersion)

tasks.withType<JavaCompile>().configureEach {
    options.release = requiredJava.majorVersion.toInt()
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget = kotlinJvmTarget
}

bloom {
    replacement("@MOD_ID@", modid)
    replacement("@MOD_NAME@", modname)
    replacement("@MOD_VERSION@", modversion)
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    processResources {
        val postEffectJson = when {
            // On 1.21.4 a pass clears its output before sampling its inputs, so a pass can never
            // read and write the same target - we ping-pong through an internal swap target instead
            // Uniforms set dynamically via PostChain.setUniform must not be listed on the pass,
            // or the static config values would override them every frame
            mcversion == "1.21.4" -> """
                {
                    "targets": {
                        "swap": {}
                    },
                    "passes": [
                        {
                            "program": "polyblur:post/phosphor_motion_blur_legacy",
                            "inputs": [
                                { "sampler_name": "Diffuse", "target": "minecraft:main" },
                                { "sampler_name": "Prev", "target": "polyblur:previous" }
                            ],
                            "output": "swap"
                        },
                        {
                            "program": "minecraft:post/blit",
                            "inputs": [
                                { "sampler_name": "In", "target": "swap" }
                            ],
                            "output": "minecraft:main"
                        }
                    ]
                }
            """.trimIndent()
            // On 1.21.5 pass uniforms declare the pipeline uniforms, so they must be listed, but
            // omitting "values" keeps them dynamic (set from code) instead of pinned to defaults
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
                                { "name": "Strength", "type": "float" },
                                { "name": "Mode", "type": "float" }
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

        val motionEffectJson = when {
            mcversion == "1.21.4" -> """
                {
                    "targets": {
                        "swap": {}
                    },
                    "passes": [
                        {
                            "program": "polyblur:post/unity_motion_blur_legacy",
                            "inputs": [
                                { "sampler_name": "Diffuse", "target": "minecraft:main" }
                            ],
                            "output": "swap"
                        },
                        {
                            "program": "minecraft:post/blit",
                            "inputs": [
                                { "sampler_name": "In", "target": "swap" }
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
                            "fragment_shader": "polyblur:post/unity_motion_blur_legacy",
                            "inputs": [
                                { "sampler_name": "Diffuse", "target": "minecraft:main" }
                            ],
                            "uniforms": [
                                { "name": "VelocityX", "type": "float" },
                                { "name": "VelocityY", "type": "float" },
                                { "name": "Samples", "type": "float" },
                                { "name": "Jitter", "type": "float" }
                            ],
                            "output": "minecraft:main"
                        }
                    ]
                }
            """.trimIndent()
            else -> ""
        }

        val props = mapOf(
            "mod_id" to modid,
            "mod_name" to modname,
            "mod_version" to modversion,
            "minecraft_version_range" to versionrange,
            "loader_version" to loaderversion,
            "java_version" to "JAVA_${requiredJava.majorVersion}"
        )

        inputs.properties(props)
        inputs.property("postEffectJson", postEffectJson)
        inputs.property("motionEffectJson", motionEffectJson)

        filesMatching(listOf("fabric.mod.json", "mixins.$modid.json")) {
            expand(props)
        }

        exclude("assets/polyblur/post_effect/phosphor_motion_blur.json")
        exclude("assets/polyblur/post_effect/unity_motion_blur.json")

        if (mcversion != "1.21.1") {
            exclude("assets/minecraft/shaders/**")
        }

        if (mcversion != "1.21.4") {
            exclude(
                "assets/polyblur/shaders/post/phosphor_motion_blur_legacy.json",
                "assets/polyblur/shaders/post/unity_motion_blur_legacy.json",
                "assets/polyblur/shaders/post/motion_velocity_legacy.json",
                "assets/polyblur/shaders/post/motion_reproject_legacy.json"
            )
        }

        doLast {
            val output = destinationDir.resolve("assets/polyblur/post_effect/phosphor_motion_blur.json")
            output.parentFile.mkdirs()
            output.writeText("$postEffectJson\n")

            if (motionEffectJson.isNotEmpty()) {
                val motionOutput = destinationDir.resolve("assets/polyblur/post_effect/unity_motion_blur.json")
                motionOutput.parentFile.mkdirs()
                motionOutput.writeText("$motionEffectJson\n")
            }
        }
    }

    jar {
        inputs.property("archivesName", base.archivesName)

        from(rootProject.file("LICENSE")) {
            rename { "${it}_${inputs.properties["archivesName"]}" }
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", modversion)
        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/$modversion"))
    }
}

val modrinthId = listOf("oneconfig.publish.modrinth", "publish.modrinth")
    .firstNotNullOfOrNull { findProperty(it) }?.toString()?.takeIf { it.isNotBlank() }
val modrinthToken = listOf("oneconfig.publish.modrinth.token", "publish.modrinth.token", "modrinth.token")
    .firstNotNullOfOrNull { findProperty(it) }?.toString()?.takeIf { it.isNotBlank() }

val changelogs = rootProject.file("CHANGELOG.md").takeIf { it.exists() }?.readText() ?: "No changelog provided."

val validateChangelog = tasks.register("validateChangelog") {
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
    file = loomx.modJar.flatMap { it.archiveFile }

    displayName = modversion
    version = "v$modversion"
    changelog = changelogs
    type = STABLE

    modLoaders.add("fabric")

    dryRun = modrinthId == null || modrinthToken == null

    if (modrinthId != null) {
        modrinth {
            projectId = modrinthId
            accessToken = modrinthToken.orEmpty()

            minecraftVersions.addAll(compatibleVersions.ifEmpty { listOf(mcversion) })

            requires("oneconfig")
            requires("fabric-language-kotlin")
        }
    }
}
