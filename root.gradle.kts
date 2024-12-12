plugins {
    id("dev.deftu.gradle.multiversion-root")
}

preprocess {
    "1.12.2-forge"(11202, "srg") {
        "1.12.2-fabric"(11202, "yarn", rootProject.file("versions/1.12.2-1.8.9.txt")) {
            "1.8.9-fabric"(10809, "yarn") {
                "1.8.9-forge"(10809, "srg")
            }
        }
    }
}