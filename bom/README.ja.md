# DevCore BOM

[[English](README.md)] | [日本語]

各モジュールのバージョンを統一するための DevCore モジュール BOM (`java-platform`)。

## 導入方法 (Gradle Kotlin DSL)

```kotlin
dependencies {
  implementation(platform("com.peco2282.devcore:devcore-bom:<version>"))
  implementation("com.peco2282.devcore:adventure")
  implementation("com.peco2282.devcore:command")
  implementation("com.peco2282.devcore:config")
  implementation("com.peco2282.devcore:scheduler")
  implementation("com.peco2282.devcore:cooldown")
}
```
