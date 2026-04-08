version = properties["devcore.database.version"] ?: "1.0.0"

repositories {
  mavenCentral()
}

dependencies {
  api(project(":adventure"))
  api("org.jetbrains.exposed:exposed-core:1.2.0")
  api("org.jetbrains.exposed:exposed-java-time:1.2.0")
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
  implementation(libs.kotlinx.coroutines)
}
