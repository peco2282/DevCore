# DevCore Adventure

Adventureライブラリ（KyoriPowered）をKotlinから使いやすくするためのDSLを提供します。

## 特徴

- 直感的なDSLによるテキストコンポーネントの構築
- 型安全なスタイリング
- 色、装飾、クリック・ホバーイベントの簡単な適用
- 各種コンポーネントの結合（join/collect）

## Install (Gradle Kotlin DSL)
```kotlin
dependencies {
  implementation("com.peco2282.devcore:adventure:<version>")
  // or:
  // implementation(platform("com.peco2282.devcore:bom:<version>"))
  // implementation("com.peco2282.devcore:adventure")
}
```

## 使用方法

### 基本的なテキスト構築

`component`ブロック内で`text`関数を使用して構築します。

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

### スタイリングDSL

`Styler`インターフェースを通じて、豊富なスタイリングメソッドを利用できます。

- **色**: `red()`, `green()`, `blue()`, `yellow()`, `color(0xFF0000)`, `color("#FF0000")`
- **装飾**: `bold()`, `italic()`, `underline()`, `strikethrough()`, `obfuscated()`
- **イベント**:
  - `runCommand("/help")`
  - `suggestCommand("/msg ")`
  - `openUrl("https://...")`
  - `copyToClipboard("text")`
  - `showText("Hover message")`
  - `showItem(key, count)`
- **その他**: `font("minecraft:default")`, `insertion("shift-click text")`

### コンポーネントの結合

複数のコンポーネントを特定の区切り文字で結合できます。

```kotlin
val list = component(joiner = { it.join(" | ") }) {
  append("Item 1")
  append("Item 2")
  append("Item 3")
}
// 結果: Item 1 | Item 2 | Item 3
```

### 高度な機能

- **条件付きスタイリング**: `whenTrue(condition) { ... }`
- **翻訳**: `translatable("key.name")`, `translatableAny("key", arg1, arg2)`
- **セレクター**: `selector("@a[distance=..5]")`
- **Keybind**: `keybind("key.jump")`
- **NBT**: `blockNbt`, `entityNbt`, `storageNbt` (Experimental)

