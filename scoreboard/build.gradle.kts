repositories {
  maven("https://repo.codemc.io/repository/maven-releases/")
}
version = properties["devcore.scoreboard.version"] ?: "1.0.0"

dependencies {
  implementation(libs.kotlin.stdlib)
  compileOnly(libs.paper.api)
  api(project(":adventure"))
  api(project(":scheduler"))
  compileOnly(project(":packet"))
  compileOnly("com.github.retrooper:packetevents-spigot:2.11.2")
  testImplementation(libs.kotlin.test)
}
