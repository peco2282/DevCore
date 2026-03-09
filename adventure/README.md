# DevCore Adventure

[Adventure](https://github.com/KyoriPowered/adventure) の `Component` / `Style` を Kotlin DSL で組み立てるためのモジュールです。

## Install (Gradle Kotlin DSL)
```kotlin
dependencies {
  implementation("com.peco2282.devcore:adventure:<version>")
  // or:
  // implementation(platform("com.peco2282.devcore:bom:<version>"))
  // implementation("com.peco2282.devcore:adventure")
}
```

## Usage
```kotlin
import com.peco2282.devcore.adventure.component

val msg = component {
  text("Hello")
  space()
  text("World") { blue(); bold() }
}
```

