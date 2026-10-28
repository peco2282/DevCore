# DevCore Adventure

[[English](README.md)] | [日本語]

Adventureライブラリ（KyoriPowered）をKotlinから使いやすくするためのDSLを提供します。

## 特徴

- 直感的なDSLによるテキストコンポーネントの構築
- 型安全なスタイリング
- 色、装飾、クリック・ホバーイベントの簡単な適用
- 各種コンポーネントの結合（join/collect）
- タイトルとボスバーの表示DSL

## Install (Gradle Kotlin DSL)

```kotlin
dependencies {
  implementation("com.peco2282.devcore:adventure:<version>")
  // または:
  // implementation(platform("com.peco2282.devcore:devcore-bom:<version>"))
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

### タイトルとボスバー

tickまたは秒単位でタイトルの表示時間を指定できます。省略した時間にはAdventureのデフォルト値が使われます。

```kotlin
player.title {
  title { text("クエスト達成！") { gold(); bold() } }
  subtitle { text("報酬を受け取れます") { green() } }
  fadeIn = 10 // tick
  staySeconds = 3.0
}

player.updateTitle(title = component { text("タイトルのみ更新") })
```

ボスバーはDSLで作成して表示できます。返されるAdventureの`BossBar`は後から更新できます。

```kotlin
val bar = player.showBossBar {
  name { text("イベント進行度") }
  progress = 0.5f
  color = BossBar.Color.BLUE
  overlay = BossBar.Overlay.PROGRESS
}

bar.update { progress = 0.75f }
player.hideBossBar(bar)
```

同じボスバーを`Collection<Audience>`全体に表示・非表示できます。

### 高度な機能

- **条件付きスタイリング**: `whenTrue(condition) { ... }`
- **翻訳**: `translatable("key.name")`, `translatableAny("key", arg1, arg2)`
- **セレクター**: `selector("@a[distance=..5]")`
- **Keybind**: `keybind("key.jump")`
- **NBT**: `blockNbt`, `entityNbt`, `storageNbt` (Experimental)
