plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.dokka)
  alias(libs.plugins.paperweight) apply false
}

group = "com.peco2282.devcore"
version = properties["devcore.version"] ?: "1.0"

// Dokka マルチモジュール設定
dependencies {
  subprojects.forEach { subproject ->
    // bom と TestPlugin 以外のモジュールを統合ドキュメントに含める
    // scoreboard-nms の子モジュールは scoreboard-nms がまとめるため除外
    if (subproject.name != "bom" && subproject.name != "core" && subproject.name != "TestPlugin" && !subproject.name.startsWith("v1_")) {
      dokka(subproject)
    }
  }
}

allprojects {
  repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
      name = "papermc-repo"
    }
  }
}

subprojects {
  group = rootProject.group

  if (name != "bom" && name != "TestPlugin" && !name.startsWith("v1_")) {
    apply(plugin = "devcore.kotlin-conventions")
    apply(plugin = "devcore.dokka-conventions")
    apply(plugin = "java-library")
  } else if (name.startsWith("v1_")) {
    apply(plugin = "devcore.kotlin-conventions")
    apply(plugin = "java-library")
  } else if (name == "bom") {
    apply(plugin = "java-platform")
  }

  if (name != "TestPlugin" && name != "scoreboard" && !name.startsWith("v1_") && !path.contains(":v1_")) {
    apply(plugin = "devcore.publish-conventions")
  }
}
