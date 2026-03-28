version = properties["devcore.adventure.version"] ?: "1.0.0"

dependencies {
  testImplementation(libs.kotlin.test)
  compileOnly(libs.adventure)
  compileOnly(libs.serializer.legacy)
  compileOnly(libs.serializer.minimessage)
  testImplementation(libs.adventure)
  testImplementation(libs.serializer.plugin)
}
