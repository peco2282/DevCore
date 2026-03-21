version = properties["devcore.scoreboard.nms.version"] ?: "1.0.0"

val nmsVersions = subprojects.filter { it.name.startsWith("v1_") }

dependencies {
  implementation(project(":scoreboard:scoreboard-api"))
  implementation(project(":adventure"))
  implementation(project(":scheduler"))
  compileOnly(libs.paper.api)

  // NMS実装はリフレクションで動的にロードされるため、コンパイル時のプロジェクト依存は不要。
  // これにより、将来的な循環参照を回避しつつ、jarタスクで成果物を集約する。

  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
}

tasks.jar {
  nmsVersions.forEach { subproject ->
    evaluationDependsOn(subproject.path) // サブプロジェクトの評価を強制する
    val reobfJar = subproject.tasks.named("reobfJar")
    from(reobfJar.map { zipTree(it.outputs.files.singleFile) }) {
      exclude("META-INF/**")
    }
  }
}
