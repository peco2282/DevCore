# DevCore Scheduler

Bukkit scheduler を扱うための薄いラッパー + DSL と、コルーチン用 `CoroutineDispatcher` を提供します。

## Install (Gradle Kotlin DSL)
```kotlin
dependencies {
  implementation("com.peco2282.devcore:scheduler:<version>")
  // or:
  // implementation(platform("com.peco2282.devcore:bom:<version>"))
  // implementation("com.peco2282.devcore:scheduler")
}
```

## Usage
```kotlin
import com.peco2282.devcore.sheduler.seconds
import com.peco2282.devcore.sheduler.taskCreate

plugin.taskCreate after 5.seconds run {
  // ...
}
```

