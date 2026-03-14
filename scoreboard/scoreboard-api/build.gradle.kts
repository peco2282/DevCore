version = properties["devcore.scoreboard.api.version"] ?: "1.0.0"

dependencies {
  implementation(project(":adventure"))
  implementation(project(":scheduler"))
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(kotlin("test"))
}
