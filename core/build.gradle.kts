plugins {
  `java-library`
}

dependencies {
  api(project(":adventure"))
  api(project(":command"))
  api(project(":config"))
  api(project(":scheduler"))
  api(project(":cooldown"))
}
