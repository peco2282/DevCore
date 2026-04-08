version = properties["devcore.database.version"] ?: "1.0.0"

dependencies {
  api(project(":database:database-api"))
  implementation("org.jetbrains.exposed:exposed-jdbc:1.0.0")
  implementation("com.zaxxer:HikariCP:7.0.2")
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
}
