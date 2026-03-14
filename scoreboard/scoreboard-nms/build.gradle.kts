plugins {
  id("io.papermc.paperweight.userdev")
}

version = properties["devcore.scoreboard.nms.version"] ?: "1.0.0"

dependencies {
  implementation(project(":scoreboard:scoreboard-api"))
  implementation(project(":scoreboard:scoreboard-nms:v1_21_4"))
  implementation(project(":adventure"))
  implementation(project(":scheduler"))
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
}

val paperVersion = "1.21.4-R0.1-SNAPSHOT"

dependencies {
  paperweight.paperDevBundle(paperVersion)
  compileOnly("io.papermc.paper:paper-api:${paperVersion}")
}
