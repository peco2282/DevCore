version = properties["devcore.database.version"] ?: "1.0.0"

dependencies {
  api(project(":database:database-api"))
  implementation(libs.exposed.jdbc)
  implementation(libs.hikari)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
}
