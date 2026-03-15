import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jetbrains.kotlin.jvm")
  id("idea")
}

kotlin {
  jvmToolchain(21)
}

idea {
  module {
    isDownloadJavadoc = true
    isDownloadSources = true
  }
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    apiVersion.set(KotlinVersion.KOTLIN_2_1)
    languageVersion.set(KotlinVersion.KOTLIN_2_1)
    jvmTarget.set(JvmTarget.JVM_21)
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}
