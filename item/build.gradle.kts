version = properties["devcore.item.version"] ?: "1.0.0"

dependencies {
  api(project(":pdc"))
  api(project(":util"))
  implementation(libs.kotlin.stdlib)
  compileOnly(libs.adventure)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.mockbukkit)
}
