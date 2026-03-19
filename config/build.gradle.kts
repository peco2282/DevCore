version = properties["devcore.config.version"] ?: "1.0.0"

dependencies {
  compileOnly(libs.adventure)
  compileOnly(libs.serializer.minimessage)
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlinx.coroutines)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
}
