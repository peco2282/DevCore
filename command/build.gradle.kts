plugins {
  kotlin("jvm")
}

group = "com.peco2282"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/") {
    name = "papermc-repo"
  }
}

dependencies {
  compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
  testImplementation(kotlin("test"))
}

kotlin {
  jvmToolchain(23)
}

tasks.test {
  useJUnitPlatform()
}
