plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "DevCore"

include(":adventure")
include(":config")
include(":scheduler")

include("command")