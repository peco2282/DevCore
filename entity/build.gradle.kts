version = properties["devcore.entity.version"] ?: "1.0.0"

val nmsVersions = subprojects.filter { it.name.startsWith("v1_") }

dependencies {
  implementation(project(":scheduler"))
  implementation(project(":event"))
  implementation(project(":util"))
  compileOnly(libs.adventure)
  compileOnly(libs.serializer.minimessage)
  implementation(libs.kotlin.stdlib)
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.mockbukkit)
}

tasks.jar {
  nmsVersions.forEach { subproject ->
    evaluationDependsOn(subproject.path) // ここでサブプロジェクトの評価を強制する
    val reobfJar = subproject.tasks.named("reobfJar")
    from(reobfJar.map { zipTree(it.outputs.files.singleFile) }) {
      exclude("META-INF/**")
    }
  }
}

