plugins {
  kotlin("jvm") version "2.2.21" apply false
  id("org.jetbrains.dokka") version "2.0.0" apply false
}

group = "com.peco2282.devcore"
version = "1.0-SNAPSHOT"

subprojects {
  group = rootProject.group
  version = rootProject.version

  repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
      name = "papermc-repo"
    }
  }

  // secrets.properties を読み込む
  val secrets = java.util.Properties().apply {
    val secretsFile = rootProject.file("secrets.properties")
    if (secretsFile.exists()) {
      secretsFile.inputStream().use { load(it) }
    }
  }

  if (name != "bom" && name != "TestPlugin") {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.dokka")
  } else if (name == "bom") {
    apply(plugin = "java-platform")
  }
  if (name != "TestPlugin") {
    apply(plugin = "maven-publish")
  }

  plugins.withId("org.jetbrains.dokka") {
    tasks.register<Jar>("dokkaJar") {
      from(tasks.named("dokkaGeneratePublicationHtml"))
      archiveClassifier.set("javadoc")
    }
  }

  plugins.withId("idea") {
    extensions.configure<org.gradle.plugins.ide.idea.model.IdeaModel>("idea") {
      module {
        isDownloadJavadoc = true
        isDownloadSources = true
      }
    }
  }

  plugins.withId("org.jetbrains.kotlin.jvm") {
    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>("kotlin") {
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
        create<MavenPublication>("maven") {
          artifactId = if (project.name == "bom") "devcore-bom" else project.name.lowercase()

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
                name.set("The MIT License")
                url.set("https://opensource.org/licenses/MIT")
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
