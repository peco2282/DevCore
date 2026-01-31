plugins {
  `java-platform`
}

javaPlatform {
  allowDependencies()
}

dependencies {
  constraints {
    api(project(":adventure"))
    api(project(":command"))
    api(project(":config"))
    api(project(":scheduler"))
    api(project(":core"))
    api(project(":cooldown"))
  }
}
