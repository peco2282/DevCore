version = properties["devcore.worldpool.version"] ?: "1.0.0"
dependencies {
  implementation(libs.kotlin.stdlib)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
}
