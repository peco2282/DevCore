plugins {
  kotlin("jvm") version "2.2.21" apply false
}

group = "com.peco2282"
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

  if (name != "bom") {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
  } else {
    apply(plugin = "java-platform")
  }
  apply(plugin = "maven-publish")

  plugins.withId("org.jetbrains.kotlin.jvm") {
    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>("kotlin") {
      jvmToolchain(23)
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
      .orElse(providers.environmentVariable("DEVCORE_PUBLISH_RELEASE_URL"))
  val publishSnapshotUrl =
    providers.gradleProperty("devcore.publish.snapshotUrl")
      .orElse(providers.environmentVariable("DEVCORE_PUBLISH_SNAPSHOT_URL"))
  val publishUser =
    providers.gradleProperty("devcore.publish.user")
      .orElse(providers.environmentVariable("DEVCORE_PUBLISH_USER"))
  val publishPassword =
    providers.gradleProperty("devcore.publish.password")
      .orElse(providers.environmentVariable("DEVCORE_PUBLISH_PASSWORD"))

  plugins.withId("maven-publish") {
    extensions.configure<PublishingExtension>("publishing") {
      publications {
        create<MavenPublication>("maven") {
          artifactId = project.name.lowercase()

          if (project.plugins.hasPlugin("java-platform")) {
            from(project.components["javaPlatform"])
          } else {
            from(project.components["java"])
          }

          pom {
            name.set(artifactId)
            description.set("DevCore Minecraft plugin library module: ${project.name}")
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
