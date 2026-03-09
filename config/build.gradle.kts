dependencies {
  compileOnly(libs.adventure)
  compileOnly(libs.serializer.minimessage)
  implementation(kotlin("stdlib"))
  implementation(libs.kotlin.reflect)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(kotlin("test"))
}
