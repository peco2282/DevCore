version = properties["devcore.scoreboard.nms.version"] ?: "1.0.0"

val nmsVersions = subprojects.filter { it.name.startsWith("v1_") }

dependencies {
  implementation(project(":scoreboard:scoreboard-api"))
  implementation(project(":adventure"))
  implementation(project(":scheduler"))
  compileOnly(libs.paper.api)

  nmsVersions.forEach {
    compileOnly(it)
    dokka(it)
  }

  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
}

tasks.jar {
  nmsVersions.forEach { subproject ->
    val reobfJar = subproject.tasks.named("reobfJar")
    from(reobfJar.map { zipTree(it.outputs.files.singleFile) }) {
        exclude("META-INF/**")
    }
  }
}
