dependencies {
  api(project(":adventure"))
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(kotlin("test"))
}
