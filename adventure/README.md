# Adventure DSL

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.21-blue.svg?logo=kotlin)](http://kotlinlang.org)

[Adventure](https://github.com/KyoriPowered/adventure) テキストコンポーネントを Kotlin で直感的に記述するための DSL ライブラリです。

## 特徴

- **宣言的な記述**: ネストされたコンポーネントを型安全で読みやすく作成できます。
- **直感的なスタイル指定**: 色や装飾（太字、斜体など）をメソッドチェーンやブロックで簡単に指定可能です。
- **Adventure 互換**: Kyori Adventure の `Component` クラスを直接生成するため、既存のプロジェクトに簡単に導入できます。
- **高度な機能のサポート**: 翻訳（Translatable）、NBT（Block/Entity）、イベント（Click/Hover）などをフルサポートしています。

## 導入方法

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://repo.peco2282.com/maven-release/")
}

dependencies {
    implementation("com.peco2282:adventure:1.0")
}
```

## 使い方

### 基本的なテキスト

```kotlin
val msg = component {
    text("Hello")
    space()
    text("World") {
        blue()
        bold()
    }
}
```

### スタイルとイベント

```kotlin
val msg = component {
    text("ここをクリック") {
        aqua()
        underline()
        clickEvent(ClickEvent.openUrl("https://example.com"))
        
        showText {
            text("ウェブサイトを開きます") { yellow() }
        }
    }
}
```

### 翻訳と引数

```kotlin
val msg = component {
    translatable("welcome.user") {
        text("Peco") { gold() }
    }
}
```

### 反復処理 (forEach)

```kotlin
val items = listOf("Apple", "Banana", "Orange")
val msg = component {
    text("Fruits: ")
    forEach(items) { item ->
        text(item)
        if (item != items.last()) text(", ")
    }
}
```

### NBT コンポーネント

```kotlin
val msg = component {
    blockNbt("Items[0].id") {
        absoluteWorldPos(10, 64, -10)
        interpret(true)
    }
}
```

## ライセンス

このプロジェクトは [Apache License 2.0](LICENSE) の下で公開されています。
