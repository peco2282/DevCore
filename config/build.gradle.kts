version = properties["devcore.config.version"] ?: "2.0.1"

dependencies {
  compileOnly(libs.adventure)
  compileOnly(libs.serializer.minimessage)
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlin.reflect)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
}
