dependencies {
  testImplementation(kotlin("test"))
  compileOnly(libs.adventure)
  testImplementation(libs.adventure)
  testImplementation(libs.serializer.plugin)
}
