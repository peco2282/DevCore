version = properties["devcore.teleport.version"] ?: "1.0.0"

dependencies {
  implementation(project(":scheduler"))
  implementation(project(":cooldown"))
  api(project(":effect"))
  implementation(libs.kotlin.stdlib)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.mockbukkit)
}
