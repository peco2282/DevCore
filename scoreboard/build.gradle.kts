version = properties["devcore.scoreboard.version"] ?: "1.0.0"

dependencies {
  implementation(libs.kotlin.stdlib)
  compileOnly(libs.paper.api)
  api(project(":adventure"))
  api(project(":scheduler"))
  testImplementation(libs.kotlin.test)
}
