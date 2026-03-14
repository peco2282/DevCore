import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.dokka)
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.19" apply false
}

group = "com.peco2282.devcore"
version = properties["devcore.version"] ?: "1.0"

// Dokka マルチモジュール設定
dependencies {
  subprojects.forEach { subproject ->
    // bom と TestPlugin 以外のモジュールを統合ドキュメントに含める
    if (subproject.name != "bom" && subproject.name != "core" && subproject.name != "TestPlugin") {
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

  // secrets.properties を読み込む
  val secrets = java.util.Properties().apply {
    val secretsFile = rootProject.file("secrets.properties")
    if (secretsFile.exists()) {
      secretsFile.inputStream().use { load(it) }
    }
  }

  if (name != "bom" && name != "TestPlugin") {
    apply(plugin = rootProject.libs.plugins.kotlin.jvm.get().pluginId)
    apply(plugin = "java-library")
    apply(plugin = rootProject.libs.plugins.idea.get().pluginId)
    apply(plugin = rootProject.libs.plugins.dokka.get().pluginId)
  } else if (name == "bom") {
    apply(plugin = "java-platform")
  }
  if (name != "TestPlugin") {
    apply(plugin = "maven-publish")
  }

  tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
      apiVersion.set(KotlinVersion.KOTLIN_2_1)
      languageVersion.set(KotlinVersion.KOTLIN_2_1)

      jvmTarget.set(JvmTarget.JVM_21)
    }
  }

  plugins.withId("org.jetbrains.dokka") {
    tasks.register<Jar>("dokkaJar") {
      from(tasks.named("dokkaGeneratePublicationHtml"))
      archiveClassifier.set("javadoc")
    }

    extensions.configure<DokkaExtension>("dokka") {
      moduleName.set(project.name)
    }
  }

  plugins.withId("idea") {
    extensions.configure<IdeaModel>("idea") {
      module {
        isDownloadJavadoc = true
        isDownloadSources = true
      }
    }
  }

  plugins.withId("org.jetbrains.kotlin.jvm") {
    extensions.configure<KotlinJvmProjectExtension>("kotlin") {
      jvmToolchain(21)
    }
  }

  tasks.withType<Test>().configureEach {
    useJUnitPlatform()
  }

  plugins.withId("java-library") {
    extensions.configure<JavaPluginExtension>("java") {
      withSourcesJar()
    }
  }

  val publishReleaseUrl =
    providers.gradleProperty("devcore.publish.releaseUrl")
      .orElse(provider { secrets.getProperty("devcore.publish.releaseUrl") })
      .orElse(providers.environmentVariable("DEVCORE_PUBLISH_RELEASE_URL"))
  val publishSnapshotUrl =
    providers.gradleProperty("devcore.publish.snapshotUrl")
      .orElse(provider { secrets.getProperty("devcore.publish.snapshotUrl") })
      .orElse(providers.environmentVariable("DEVCORE_PUBLISH_SNAPSHOT_URL"))
  val publishUser =
    providers.gradleProperty("devcore.publish.user")
      .orElse(provider { secrets.getProperty("devcore.publish.user") })
      .orElse(providers.environmentVariable("DEVCORE_PUBLISH_USER"))
  val publishPassword =
    providers.gradleProperty("devcore.publish.password")
      .orElse(provider { secrets.getProperty("devcore.publish.password") })
      .orElse(providers.environmentVariable("DEVCORE_PUBLISH_PASSWORD"))

  plugins.withId("maven-publish") {
    extensions.configure<PublishingExtension>("publishing") {
      publications {
        afterEvaluate {
          create<MavenPublication>("maven") {
            artifactId = if (project.name == "bom") "devcore-bom" else project.name.lowercase()

            // 各モジュールで個別に version が定義される
            version = project.version.toString()

            if (project.plugins.hasPlugin("java-platform")) {
              from(project.components["javaPlatform"])
            } else if (project.plugins.hasPlugin("java-library") || project.plugins.hasPlugin("java")) {
              from(project.components["java"])
              artifact(tasks.named("dokkaJar"))
            }

            pom {
              name.set(artifactId)
              description.set("DevCore Minecraft plugin library module: ${project.name}")
              url.set("https://github.com/peco2282/DevCore")

              licenses {
                license {
                  name.set("Apache License, Version 2.0")
                  url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
              }

              developers {
                developer {
                  id.set("peco2282")
                  name.set("peco2282")
                }
              }

              scm {
                connection.set("scm:git:github.com/peco2282/DevCore.git")
                developerConnection.set("scm:git:ssh://github.com/peco2282/DevCore.git")
                url.set("https://github.com/peco2282/DevCore/tree/main")
              }
            }
          }
        }
      }

      repositories {
        val targetUrlProvider =
          if (version.toString().endsWith("SNAPSHOT")) publishSnapshotUrl else publishReleaseUrl

        val targetUrl = targetUrlProvider.orNull
        if (!targetUrl.isNullOrBlank()) {
          maven {
            name = "devcore"
            url = uri(targetUrl)
            credentials {
              username = publishUser.orNull
              password = publishPassword.orNull
            }
          }
        }
      }
    }
  }
}
