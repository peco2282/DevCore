version = properties["devcore.command.version"] ?: "1.0.0"

dependencies {
  api(project(":adventure"))
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
}
