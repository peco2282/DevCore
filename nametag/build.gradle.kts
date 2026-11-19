version = properties["devcore.nametag.version"] ?: "1.0.0"

dependencies {
  implementation(project(":adventure"))
  implementation(libs.kotlin.stdlib)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.mockbukkit)
}
