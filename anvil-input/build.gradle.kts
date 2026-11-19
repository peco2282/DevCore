version = properties["devcore.anvil.input.version"] ?: "1.0.0"

dependencies {
  api(project(":adventure"))
  implementation(project(":util"))
  implementation(libs.kotlin.stdlib)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
}
