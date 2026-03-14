version = properties["devcore.core.version"] ?: "1.0.0"

dependencies {
  api(project(":adventure"))
  api(project(":command"))
  api(project(":config"))
  api(project(":scheduler"))
  api(project(":event"))
  api(project(":cooldown"))
  api(project(":gui"))
  api(project(":scoreboard:scoreboard-lite"))
}
