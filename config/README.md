# DevCore Config

`config.yml` 等の YAML を Kotlin のクラスへマッピングし、デフォルト値の書き戻しや簡単なバリデーションを行うモジュールです。

## Install (Gradle Kotlin DSL)
```kotlin
dependencies {
  implementation("com.peco2282.devcore:config:<version>")
  // or:
  // implementation(platform("com.peco2282.devcore:bom:<version>"))
  // implementation("com.peco2282.devcore:config")
}
```

## Usage
```kotlin
import com.peco2282.devcore.config.Configs

data class MyConfig(
  val name: String = "Steve",
  val level: Int = 1,
)

val cfg: MyConfig = Configs.load<MyConfig>(plugin)
```

