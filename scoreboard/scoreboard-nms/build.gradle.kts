version = properties["devcore.scoreboard.nms.version"] ?: "1.0.0"

dependencies {
  implementation(project(":scoreboard:scoreboard-api"))
  implementation(project(":adventure"))
  implementation(project(":scheduler"))
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
}
