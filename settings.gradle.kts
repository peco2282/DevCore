pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
  }
}
plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
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
include("scoreboard:scoreboard-nms:v1_20_2")
include("scoreboard:scoreboard-nms:v1_20_3")
include("packet:v1_21_4")
include("packet:v1_21_11")
include("packet:v1_20_4")
include("packet:v1_20_6")
include("world")
include("entity")
include("util")
include("database:database-api")
include("database:database-jdbc")