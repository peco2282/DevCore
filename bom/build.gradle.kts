version = properties["devcore.bom.version"] ?: "1.0.0"

javaPlatform {
  allowDependencies()
}

fun release(modPath: String): Any {
  if (properties["devcore.dev"] == "true") {
    return project(":$modPath")
  }
  val modName = modPath.split(":").last()
  val prefix = "devcore.${modName.replace("-", ".")}."
  val rel = properties["${prefix}release"] ?: properties["${prefix}version"]
  ?: throw IllegalArgumentException("No release version for $modPath")
  return "com.peco2282.devcore:$modName:$rel"
}

dependencies {
  constraints {
    api(release("core"))
    api(release("command"))
    api(release("config"))
    api(release("scheduler"))
    api(release("adventure"))
    api(release("cooldown"))
    api(release("gui"))
    api(release("event"))
    api(release("effect"))
    api(release("packet"))
    api(release("scoreboard:scoreboard-api"))
    api(release("scoreboard:scoreboard-lite"))
    api(release("scoreboard:scoreboard-nms"))
  }
}
