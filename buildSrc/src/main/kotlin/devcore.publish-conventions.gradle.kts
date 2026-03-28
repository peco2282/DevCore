import java.net.HttpURLConnection

plugins {
  `maven-publish`
}

val secrets = java.util.Properties().apply {
  val secretsFile = rootProject.file("secrets.properties")
  if (secretsFile.exists()) {
    secretsFile.inputStream().use { load(it) }
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

val RED = "\u001b[31m"
val GREEN = "\u001b[32m"
val YELLOW = "\u001b[33m"
val CYAN = "\u001b[36m"
val RESET = "\u001b[0m"
val BOLD = "\u001b[1m"


fun checkLog(project: Project, remoteUrl: String) {
  logger.lifecycle("${CYAN}[Check]${RESET} ${project.group}: $remoteUrl")
}

fun publishLog(project: Project) {
  logger.lifecycle("${BOLD}${GREEN}>>> [Publish]${RESET} ${project.name}:${project.version} will be published.")}

fun skipLog(project: Project) {
  logger.lifecycle(">>> ${BOLD}[Skip]${RESET} ${project.name}:${project.version} already exists.")}

fun warnLog(e: Exception) {
  logger.lifecycle("${YELLOW}>>> [Warn] ${e.message}. try to publish.${RESET}")
}


tasks.withType<PublishToMavenRepository>().configureEach {
  onlyIf {
    if (project.name.lowercase().contains("test")) return@onlyIf false
    if (project.name == "scoreboard") return@onlyIf false
    val artifactId = if (project.name == "bom") "devcore-bom" else project.name.lowercase()
    val groupPath = project.group.toString().replace(".", "/")
    val remoteUrl =
      "${repository.url}${groupPath}/${artifactId}/${project.version}/${artifactId}-${project.version}.pom"

    checkLog(project, remoteUrl)

    try {
      val connection = uri(remoteUrl).toURL().openConnection() as HttpURLConnection
      connection.requestMethod = "HEAD"
      connection.connectTimeout = 2000
      connection.readTimeout = 2000

      val responseCode = connection.responseCode

      if (responseCode == 200) {
        skipLog(project)
        false
      } else {
        publishLog(project)
        true
      }
    } catch (e: Exception) {
      warnLog(e)
      true
    }
  }
}

publishing {
  publications {
    afterEvaluate {
      register<MavenPublication>("maven") {
        artifactId = if (project.name == "bom") "devcore-bom" else project.name.lowercase()

        if (project.plugins.hasPlugin("java-platform")) {
          from(project.components["javaPlatform"])
        } else if (project.plugins.hasPlugin("java-library") || project.plugins.hasPlugin("java")) {
          // Workaround for KGP 2.x + Gradle 9.3 incompatibility
          artifact(tasks.named("jar"))
          if (project.plugins.hasPlugin("org.jetbrains.dokka")) {
            artifact(tasks.named("dokkaJar"))
          }
          val javaExtension = project.extensions.getByType<JavaPluginExtension>()
          javaExtension.withSourcesJar()

          pom.withXml {
            val dependenciesNode = asNode().appendNode("dependencies")
            project.configurations.findByName("api")?.allDependencies?.forEach {
              if (it is ProjectDependency) {
                val depNode = dependenciesNode.appendNode("dependency")
                depNode.appendNode("groupId", project.group)
                depNode.appendNode("artifactId", it.name.lowercase())
                depNode.appendNode("version", it.version ?: project.version)
                depNode.appendNode("scope", "compile")
              } else if (it is ExternalModuleDependency) {
                val depNode = dependenciesNode.appendNode("dependency")
                depNode.appendNode("groupId", it.group)
                depNode.appendNode("artifactId", it.name)
                depNode.appendNode("version", it.version)
                depNode.appendNode("scope", "compile")
              }
            }
            project.configurations.findByName("implementation")?.allDependencies?.forEach {
              if (it is ProjectDependency) {
                val depNode = dependenciesNode.appendNode("dependency")
                depNode.appendNode("groupId", project.group)
                depNode.appendNode("artifactId", it.name.lowercase())
                depNode.appendNode("version", it.version ?: project.version)
                depNode.appendNode("scope", "runtime")
              } else if (it is ExternalModuleDependency) {
                val depNode = dependenciesNode.appendNode("dependency")
                depNode.appendNode("groupId", it.group)
                depNode.appendNode("artifactId", it.name)
                depNode.appendNode("version", it.version)
                depNode.appendNode("scope", "runtime")
              }
            }
          }
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
      if (project.version.toString().endsWith("SNAPSHOT")) publishSnapshotUrl else publishReleaseUrl

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
