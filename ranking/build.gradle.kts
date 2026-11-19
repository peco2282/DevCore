version = properties["devcore.ranking.version"] ?: "1.0.0"
dependencies {
  implementation(libs.kotlin.stdlib)
  compileOnly(libs.paper.api)
  testImplementation(libs.kotlin.test)
}
