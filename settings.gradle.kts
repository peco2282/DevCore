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