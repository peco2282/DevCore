dependencies {
  testImplementation(kotlin("test"))
  compileOnly(libs.adventure)
  compileOnly(libs.serializer.legacy)
  testImplementation(libs.adventure)
  testImplementation(libs.serializer.plugin)
}
