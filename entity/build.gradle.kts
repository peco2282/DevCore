version = properties["devcore.entity.version"] ?: "1.0.0"

dependencies {
  implementation(project(":scheduler"))
  implementation(project(":event"))
  compileOnly(libs.adventure)
  compileOnly(libs.serializer.minimessage)
  implementation(libs.kotlin.stdlib)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
}
