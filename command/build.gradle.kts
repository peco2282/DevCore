version = properties["devcore.command.version"] ?: "1.0.0"

dependencies {
  api(project(":adventure"))
  implementation(project(":util"))
  implementation(libs.kotlinx.coroutines)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
  testImplementation(project(":command:v1_20_6"))
  testImplementation(project(":command:v1_21_1"))
}

val nmsVersions = subprojects.filter { it.name.startsWith("v1_") }

tasks.jar {
  nmsVersions.forEach { subproject ->
    evaluationDependsOn(subproject.path)
    val reobfJar = subproject.tasks.named("reobfJar")
    from(reobfJar.map { zipTree(it.outputs.files.singleFile) }) {
      exclude("META-INF/**")
    }
  }
}
