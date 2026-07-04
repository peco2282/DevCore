version = properties["devcore.task.sequence.version"] ?: "1.0.0"

dependencies {
  api(project(":scheduler"))
  compileOnly(project(":packet"))
  compileOnly(libs.paper.api)
  implementation(libs.kotlinx.coroutines)
  testImplementation(libs.paper.api)
  testImplementation(kotlin("test"))
}
