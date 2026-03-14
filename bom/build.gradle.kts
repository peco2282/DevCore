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
    api(project(":gui"))
    api(project(":event"))
    api(project(":effect"))
    api(project(":packet"))
    api(project(":scoreboard:scoreboard-api"))
    api(project(":scoreboard:scoreboard-lite"))
    api(project(":scoreboard:scoreboard-nms"))
  }
}
