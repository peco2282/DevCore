import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class PublicationConsumerTest {
  @TempDir
  lateinit var directory: Path

  @Test
  fun `published artifacts resolve api implementation runtime and exclusions from a separate consumer`() {
    val producer = directory.resolve("producer").toFile().apply { mkdirs() }
    producer.resolve("settings.gradle").writeText("rootProject.name = 'fixture'\ninclude 'apiDep', 'implDep', 'runtimeDep', 'excluded', 'nonTransitive'")
    producer.resolve("build.gradle").writeText("""
      plugins {
        id 'java-library'
        id 'devcore.publish-conventions'
      }
      allprojects {
        group = 'fixture'
        version = '1.0'
      }
      subprojects {
        apply plugin: 'java-library'
        apply plugin: 'maven-publish'
        publishing {
          publications { maven(MavenPublication) { artifactId = project.name.toLowerCase(); from components.java } }
          repositories { maven { url = rootProject.layout.buildDirectory.dir('repo') } }
        }
      }
      project(':apiDep') { dependencies { api project(':excluded') } }
      project(':implDep') { dependencies { api project(':nonTransitive') } }
      dependencies {
        api(project(':apiDep')) { exclude group: 'fixture', module: 'excluded' }
        implementation(project(':implDep')) { transitive = false }
        runtimeOnly project(':runtimeDep')
      }
      publishing { repositories { maven { url = layout.buildDirectory.dir('repo') } } }
    """.trimIndent())
    listOf("apiDep", "implDep", "runtimeDep", "excluded", "nonTransitive").forEach {
      producer.resolve(it).mkdirs()
    }
    GradleRunner.create().withProjectDir(producer).withPluginClasspath()
      .withArguments("publish", "--stacktrace").build()
    val repeated = GradleRunner.create().withProjectDir(producer).withPluginClasspath()
      .withArguments("publish", "--stacktrace").build()
    assertEquals(TaskOutcome.SKIPPED, repeated.task(":publishMavenPublicationToMavenRepository")?.outcome)
    val consumer = directory.resolve("consumer").toFile().apply { mkdirs() }
    consumer.resolve("settings.gradle").writeText("rootProject.name = 'consumer'")
    consumer.resolve("build.gradle").writeText("""
      plugins { id 'java' }
      repositories { maven { url = uri('${producer.resolve("build/repo").toURI()}') } }
      dependencies { implementation 'fixture:fixture:1.0' }
      tasks.register('verify') {
        doLast {
          def compileNames = configurations.compileClasspath.files*.name.sort()
          def runtimeNames = configurations.runtimeClasspath.files*.name.sort()
          assert compileNames == ['apidep-1.0.jar', 'fixture-1.0.jar']
          assert runtimeNames == ['apidep-1.0.jar', 'fixture-1.0.jar', 'impldep-1.0.jar', 'runtimedep-1.0.jar']
          println 'Consumer dependency verification passed'
        }
      }
    """.trimIndent())
    val result = GradleRunner.create().withProjectDir(consumer).withArguments("verify", "--stacktrace").build()
    assertTrue(result.output.contains("Consumer dependency verification passed"))
  }
}