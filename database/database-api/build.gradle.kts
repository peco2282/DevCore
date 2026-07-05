version = properties["devcore.database.version"] ?: "1.0.0"

repositories {
  mavenCentral()
}

dependencies {
  api(project(":adventure"))
  api(libs.exposed.core)
  api(libs.exposed.jdbc)
  api(libs.exposed.dao)
  api(libs.exposed.java.time)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
  implementation(libs.kotlinx.coroutines)
}
