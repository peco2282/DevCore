plugins {
  id("io.papermc.paperweight.userdev")
}

version = properties["devcore.packet.version"] ?: "1.0.0"

dependencies {
  implementation(project(":packet"))
  implementation(libs.kotlin.stdlib)
  paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
  testImplementation(libs.kotlin.test)
}

