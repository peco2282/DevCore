version = properties["devcore.effect.version"] ?: "1.0.0"

dependencies {
  api(project(":scheduler"))
  implementation(libs.kotlin.stdlib)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.mockbukkit)
}
