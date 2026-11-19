version = properties["devcore.game.version"] ?: "1.0.0"

dependencies {
  implementation(project(":adventure"))
  implementation(project(":scheduler"))
  implementation(project(":region"))
  implementation(libs.kotlin.stdlib)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.mockbukkit)
}
