
version = properties["devcore.bom.version"] ?: "1.0.0"

javaPlatform {
  allowDependencies()
}

dependencies {
  constraints {
    api(project(":core"))
    api(project(":command"))
    api(project(":config"))
    api(project(":scheduler"))
    api(project(":adventure"))
    api(project(":cooldown"))
  }
}
