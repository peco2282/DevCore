plugins {
  id("io.papermc.paperweight.userdev")
}

version = properties["devcore.scoreboard.nms.version"] ?: "1.0.0"

dependencies {
  implementation(project(":scoreboard:scoreboard-api"))
  implementation(project(":adventure"))
  implementation(project(":scheduler"))
  paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
  testImplementation(libs.kotlin.test)
}
