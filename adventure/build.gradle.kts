plugins {
  kotlin("jvm")
  alias(libs.plugins.idea)
}

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(kotlin("test"))
  compileOnly(libs.adventure)
  testImplementation(libs.adventure)
  testImplementation(libs.serializer.plugin)
}

kotlin {
  jvmToolchain(23)
}

tasks.test {
  useJUnitPlatform()
}

idea {
  module {
    isDownloadJavadoc = true
    isDownloadSources = true
  }
}
