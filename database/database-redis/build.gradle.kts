version = properties["devcore.database.version"] ?: "1.0.0"

repositories {
  mavenCentral()
}

dependencies {
  api(project(":database:database-api"))
  api(libs.jedis)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
  implementation(libs.kotlinx.coroutines)
}
