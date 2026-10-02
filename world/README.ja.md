# DevCore World

[[English](README.md)] | [日本語]

Bukkitのワールド、チャンク、ブロック、プレイヤーを型安全に編集するKotlin DSLです。

## 導入方法

```kotlin
dependencies { implementation("com.peco2282.devcore:world:<version>") }
```

## 使用例

```kotlin
world.edit {
  time = 6_000
  weather = WeatherType.CLEAR
  block(0, 64, 0) { material = Material.STONE }
}
```
