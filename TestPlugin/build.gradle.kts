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
  implementation(project(":adventure"))
  implementation(project(":gui"))
  implementation(project(":cooldown"))
  implementation(project(":packet"))
  implementation(project(":event"))
  implementation(project(":scheduler"))
  implementation(project(":scoreboard:scoreboard-api"))
  implementation(project(":scoreboard:scoreboard-nms"))
  implementation(project(":world"))
  implementation(project(":entity"))
  implementation(project(":effect"))
  implementation(project(":pdc"))
  implementation(project(":item"))
  implementation(project(":storage"))
  implementation(project(":display"))
  implementation(project(":region"))
  implementation(project(":teleport"))
  implementation(project(":game"))
  implementation(project(":loot"))
  implementation(project(":merchant"))
  implementation(project(":nametag"))
  implementation(project(":kit"))
  implementation(project(":worldpool"))
  implementation(project(":cache"))
  implementation(project(":permission"))
  implementation(project(":debug"))
  implementation(project(":ranking"))
  implementation(project(":persistent-cooldown"))
  implementation(project(":update-checker"))
  implementation(project(":anvil-input"))
  runtimeOnly(project(":anvil-input:v1_20_6"))
  runtimeOnly(project(":anvil-input:v1_21_4"))
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
