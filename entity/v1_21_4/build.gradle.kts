plugins {
  id("io.papermc.paperweight.userdev")
}

version = properties["devcore.entity.version"] ?: "1.0.0"

dependencies {
  implementation(project(":packet"))
  implementation(project(":scheduler"))
  implementation(project(":entity"))
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlinx.coroutines)
  paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
  testImplementation(libs.kotlin.test)
}

