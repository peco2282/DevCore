plugins {
  kotlin("jvm")
}

group = "com.peco2282.devcore"
version = "1.0.0"

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":adventure"))
  implementation(project(":scheduler"))
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