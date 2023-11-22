plugins {
    kotlin("jvm") version "1.8.22" apply false
    id("org.polyfrost.multi-version.root")
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

preprocess {
    "1.12.2-forge"(11202, "srg") {
        "1.8.9-forge"(10809, "srg", rootProject.file("versions/1.12.2-1.8.9.txt"))
    }
}