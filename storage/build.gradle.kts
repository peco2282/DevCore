version = properties["devcore.storage.version"] ?: "1.0.0"

dependencies {
  implementation(project(":config"))
  implementation(project(":scheduler"))
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlin.reflect)
  compileOnly(libs.adventure)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.mockbukkit)
}
