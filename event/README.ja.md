# DevCore Event

[[English](README.md)] | [日本語]

Bukkitイベントを簡潔かつ型安全に定義するためのDSL。

## 特徴

- 直感的なDSLによるイベント登録
- フィルタや条件指定が容易
- 自動解除（unregister）をサポート
- 複数のイベントのグループ化

## 導入方法 (Gradle Kotlin DSL)

```kotlin
dependencies {
  implementation("com.peco2282.devcore:event:<version>")
}
```

## 使用方法

### 基本的なイベントリスナー

```kotlin
on<PlayerJoinEvent> {
  handle {
    player.sendMessage("Welcome!")
  }
}
```

### 高度な使用方法

```kotlin
on<PlayerInteractEvent> {
  filter { action.isLeftClick }
  once() // 一度だけ実行
  handle {
    player.sendMessage("Left clicked!")
  }
}
```

### イベントグループ

```kotlin
val group = events {
  on<PlayerJoinEvent> {
    handle { player.sendMessage("Hello!") }
  }
  on<PlayerQuitEvent> {
    handle { println("${player.name} left.") }
  }
}

// 登録解除
group.unregisterAll()
```
