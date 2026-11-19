version = properties["devcore.database.version"] ?: "1.0.0"

repositories {
  mavenCentral()
}

dependencies {
  api(project(":database:database-api"))
  api(project(":database:database-jdbc"))
  api(libs.exposed.core)
  api(libs.exposed.jdbc)
  api(libs.exposed.dao)
  api(libs.exposed.java.time)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
  testRuntimeOnly("org.xerial:sqlite-jdbc:3.49.1.0") // provided by Paper at runtime
  implementation(libs.kotlinx.coroutines)
}
