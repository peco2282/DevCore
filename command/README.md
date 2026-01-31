# DevCore Command

Paper (Brigadier) コマンドを Kotlin DSL で定義するためのモジュールです。

## Install (Gradle Kotlin DSL)
```kotlin
dependencies {
  implementation("com.peco2282.devcore:command:<version>")
  // or:
  // implementation(platform("com.peco2282.devcore:bom:<version>"))
  // implementation("com.peco2282.devcore:command")
}
```

## Usage
```kotlin
import com.peco2282.devcore.command.command

plugin.command("test") {
  requires { it.sender.isOp }
  literal("sub") {
    executes { 1 }
  }
}
```

