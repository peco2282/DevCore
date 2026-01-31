plugins {
  kotlin("jvm")
}

dependencies {
  implementation(kotlin("stdlib"))
  compileOnly(libs.paper.api)
  testImplementation(kotlin("test"))
}
