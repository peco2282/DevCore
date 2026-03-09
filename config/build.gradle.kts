plugins {
  kotlin("jvm")
}

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/") {
    name = "papermc-repo"
  }
}

dependencies {
  compileOnly(libs.adventure)
  compileOnly(libs.serializer.minimessage)
  implementation(kotlin("stdlib"))
  implementation(libs.kotlin.reflect)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(kotlin("test"))
}

kotlin {
  jvmToolchain(21)
}

tasks.test {
  useJUnitPlatform()
}
