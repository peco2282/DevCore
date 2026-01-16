import java.util.Properties

plugins {
  kotlin("jvm") version "2.2.21"
  alias(libs.plugins.idea)
  `maven-publish`
}

group = "com.peco2282"
version = "1.0"

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(kotlin("test"))
  compileOnly(libs.adventure)
  testImplementation(libs.adventure)
  testImplementation(libs.serializer.plugin)
  compileOnly("com.peco2282:adventure:1.0")
}

kotlin {
  jvmToolchain(23)
}

tasks.test {
  useJUnitPlatform()
}

idea {
  module {
    isDownloadJavadoc = true
    isDownloadSources = true
  }
}

val sourcesJar by tasks.registering(Jar::class, fun Jar.() {
  group = JavaBasePlugin.BUILD_TASK_NAME
  archiveClassifier.set("sources")
  from(sourceSets.main.get().allSource)
})

val javadocJar by tasks.registering(Jar::class, fun Jar.() {
  group = JavaBasePlugin.BUILD_TASK_NAME
  archiveClassifier.set("javadoc")
  val javadocTask = tasks.named("javadoc", Javadoc::class).get()
  dependsOn(javadocTask)
  from(javadocTask.destinationDir)
})


val secrets = Properties()
rootProject.file("secrets.properties").let {
  if (it.exists()) secrets.load(it.inputStream())
}

val owningRepo = "peco2282/Adventure"

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])
      artifact(sourcesJar)
      artifact(javadocJar)

      pom {
        name.set(project.name.lowercase())
        artifactId = project.name.lowercase()
        description.set("Dice-API for TatamiServer")
        url.set("https://github.com/${owningRepo}")

        licenses {
          license {
            name.set("The Apache Software License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
          }
        }
        developers {
          developer {
            id.set("peco2282")
            name.set("peco2282")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/${owningRepo}.git")
          developerConnection.set("scm:git:ssh://github.com/${owningRepo}.git")
          url.set("https://github.com/${owningRepo}")
        }
      }
    }
  }
  repositories {
    maven {
      name = "repo"
      url = uri("https://repo.peco2282.com/maven-release/")
      credentials {
        username = secrets["username"] as String?
        password = secrets["password"] as String?
      }
    }
  }
  repositories {
    maven {
      name = "maven"
      url = uri("https://maven.peco2282.com/repository/maven-releases/")
      credentials {
        username = secrets["username"] as String?
        password = secrets["password"] as String?
      }
    }
  }
}
