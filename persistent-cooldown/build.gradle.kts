version = properties["devcore.persistent.cooldown.version"] ?: "1.0.0"
dependencies {
  api(project(":cooldown"))
  implementation(project(":pdc"))
  implementation(libs.kotlin.stdlib)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.mockbukkit)
}
