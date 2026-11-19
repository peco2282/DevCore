version = properties["devcore.cache.version"] ?: "1.0.0"
dependencies {
  implementation(project(":cooldown"))
  implementation(libs.kotlin.stdlib)
  testImplementation(libs.kotlin.test)
}
