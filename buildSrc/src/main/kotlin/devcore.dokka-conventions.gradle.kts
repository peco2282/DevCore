plugins {
  id("org.jetbrains.dokka")
}

tasks.register<Jar>("dokkaJar") {
  from(tasks.named("dokkaGeneratePublicationHtml"))
  archiveClassifier.set("javadoc")
}

dokka {
  moduleName.set(project.name)

  dokkaSourceSets {
    configureEach {
      enableAndroidDocumentationLink.set(false)
    }
  }
}
