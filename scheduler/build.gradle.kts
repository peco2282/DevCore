dependencies {
  compileOnly(libs.adventure)
  compileOnly(libs.serializer.minimessage)
  implementation(kotlin("stdlib"))
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  implementation(libs.kotlinx.coroutines)
  testImplementation(kotlin("test"))
}
