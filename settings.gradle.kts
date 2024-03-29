rootProject.name = "SuperAdvancements"

include(":superadvancements")
project(":superadvancements").projectDir = rootDir.resolve("base")

listOf("abstract", "spigot", "paper").forEach {
    include(":superadvancements-$it")
    project(":superadvancements-$it").projectDir = rootDir.resolve(it)
}

listOf(
    "1_12_R1",
    "1_13_R1",
    "1_13_R2",
    "1_14_R1",
    "1_15_R1",
    "1_16_R1",
    "1_16_R2",
    "1_16_R3",
    "1_17_R1",
    "1_18_R1",
    "1_18_R2",
    "1_19_R1",
    "1_19_R2",
    "1_19_R3",
    "1_20_R1",
    "1_20_R2",
    "1_20_R3"
).forEach {
    include(":superadvancements-$it")
    project(":superadvancements-$it").projectDir = rootDir.resolve("nms/$it")
}