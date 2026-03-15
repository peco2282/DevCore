plugins {
  id("org.jetbrains.kotlin.jvm")
  alias(libs.plugins.shadow)
  alias(libs.plugins.runPaper)
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
  compileOnly(libs.paper.api)
  implementation(libs.kotlin.stdlib)

  // BOMを使用してバージョンを一括管理
  implementation(platform(project(":bom")))

  // バージョン指定なしで依存関係を追加
  implementation(project(":core"))
  implementation(project(":command"))
  implementation(project(":config"))
  implementation(project(":scoreboard:scoreboard-api"))
  implementation(project(":scoreboard:scoreboard-nms"))
  implementation(project(":scoreboard:scoreboard-nms:v1_21_4"))
}

tasks {
  runServer {
    // Configure the Minecraft version for our task.
    // This is the only required configuration besides applying the plugin.
    // Your plugin's jar (or shadowJar if present) will be used automatically.
    minecraftVersion("1.21.4")
  }
}

val targetJavaVersion = 21
kotlin {
  jvmToolchain(targetJavaVersion)
}

tasks.build {
  dependsOn("shadowJar")
}

tasks.processResources {
  val props = mapOf("version" to version)
  inputs.properties(props)
  filteringCharset = "UTF-8"
  filesMatching("plugin.yml") {
    expand(props)
  }
}
