# DevCore Adventure
[English] | [[日本語](README.ja.md)]

Provides a DSL for making the Adventure library (KyoriPowered) easier to use from Kotlin.

## Features

- Construct text components using an intuitive DSL
- Type-safe styling
- Easy application of colors, decorations, click, and hover events
- Joining and collecting various components

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

### Basic Text Construction

Construct within the `component` block using the `text` function.

```kotlin
val msg = component {
  text("Hello")
  space()
  text("World") { 
    blue()
    bold()
    italic()
  }
}
```

### Styling DSL

A wide range of styling methods are available through the `Styler` interface.

- **Colors**: `red()`, `green()`, `blue()`, `yellow()`, `color(0xFF0000)`, `color("#FF0000")`
- **Decorations**: `bold()`, `italic()`, `underline()`, `strikethrough()`, `obfuscated()`
- **Events**:
  - `runCommand("/help")`
  - `suggestCommand("/msg ")`
  - `openUrl("https://...")`
  - `copyToClipboard("text")`
  - `showText("Hover message")`
  - `showItem(key, count)`
- **Other**: `font("minecraft:default")`, `insertion("shift-click text")`

### Joining Components

You can join multiple components with a specific separator.

```kotlin
val list = component(joiner = { it.join(" | ") }) {
  append("Item 1")
  append("Item 2")
  append("Item 3")
}
// Result: Item 1 | Item 2 | Item 3
```

### Advanced Features

- **Conditional Styling**: `whenTrue(condition) { ... }`
- **Translation**: `translatable("key.name")`, `translatableAny("key", arg1, arg2)`
- **Selectors**: `selector("@a[distance=..5]")`
- **Keybind**: `keybind("key.jump")`
- **NBT**: `blockNbt`, `entityNbt`, `storageNbt` (Experimental)

