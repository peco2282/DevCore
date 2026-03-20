plugins {
  id("io.papermc.paperweight.userdev")
}

version = properties["devcore.packet.version"] ?: "1.0.0"

dependencies {
  implementation(project(":packet"))
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlinx.coroutines)
  implementation(project(":scheduler"))
  paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
  testImplementation(libs.kotlin.test)
}

