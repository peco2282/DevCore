version = properties["devcore.core.version"] ?: "1.0.0"

dependencies {
  api(project(":adventure"))
  api(project(":command"))
  api(project(":config"))
  api(project(":scheduler"))
  api(project(":cooldown"))
  api(project(":scoreboard"))
}
