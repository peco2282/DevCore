# DevCore Packet

[[English](README.md)] | [日本語]

PacketEventsを利用したフェイクエンティティやパケット操作のためのDSL。

## 特徴

- フェイクエンティティの生成を直感的なDSLで記述
- フェイクブロック（偽のブロック表示）をサポート
- PacketEventsとの統合による高性能なパケット処理

## 導入方法 (Gradle Kotlin DSL)

```kotlin
dependencies {
  implementation("com.peco2282.devcore:packet:<version>")
}
```

## 使用方法

### フェイクエンティティ DSL

```kotlin
player.sendFakeVisuals {
  spawnEntity(EntityType.ZOMBIE, location) {
    customName = "Fake Boss"
    isGlowing = true
    equipment {
      helmet = ItemStack(Material.GOLDEN_HELMET)
    }
  }
}
```

### フェイクブロック

```kotlin
player.sendFakeVisuals {
  setFakeBlock(location.add(0.0, 1.0, 0.0), Material.DIAMOND_BLOCK)
}
```
