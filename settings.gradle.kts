pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
  }
}
plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "DevCore"

include(":adventure")
include(":config")
include(":scheduler")
include(":core")
include(":bom")
include(":cooldown")

include("command")
include("TestPlugin")

include(":gui")
include(":event")

include("effect")
include("packet")
include("scoreboard:scoreboard-lite")
include("scoreboard:scoreboard-api")
include("scoreboard:scoreboard-nms")
include("scoreboard:scoreboard-nms:v1_21_4")
include("packet:v1_21_4")
include("packet:v1_21_11")
include("packet:v1_20_6")
include("world")
include("entity")
include("util")
include("database:database-api")
include("database:database-jdbc")
include("command:v1_20_6")
include("command:v1_21_1")