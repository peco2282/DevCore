version = properties["devcore.packet.version"] ?: "1.0.0"

val nmsVersions = subprojects.filter { it.name.startsWith("v1_") }

dependencies {
  implementation(project(":scheduler"))
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlinx.coroutines)
  // NMS実装はリフレクションで動的にロードされるため、コンパイル時のプロジェクト依存は不要。
  // これにより循環参照を回避しつつ、jarタスクで成果物を集約する。

  compileOnly(libs.paper.api.common)
  compileOnly("io.netty:netty-all:4.1.100.Final")
  testImplementation(libs.kotlin.test)
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
