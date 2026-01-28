plugins {
  kotlin("jvm") version "2.2.21"
}

group = "com.peco2282"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(kotlin("test"))
}

kotlin {
  jvmToolchain(23)
}

tasks.test {
  useJUnitPlatform()
}
