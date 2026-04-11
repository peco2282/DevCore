plugins {
  id("io.papermc.paperweight.userdev")
}

version = properties["devcore.command.version"] ?: "1.0.0"

dependencies {
  implementation(project(":command"))
  implementation(libs.kotlin.stdlib)
  paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
  testImplementation(libs.kotlin.test)
}
