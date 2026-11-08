# DevCore Core

[[English](README.md)] | [日本語]

よく使う DevCore モジュールをまとめて利用するための “umbrella” artifact です。

## Install (Gradle Kotlin DSL)

```kotlin
dependencies {
  implementation("com.peco2282.devcore:core:<version>")
}
```

## 含まれるモジュール

`adventure`, `command`, `config`, `scheduler`, `task-sequence`, `event`, `cooldown`, `gui`, `scoreboard-lite`, `effect`

## 含まれないモジュール

以下は別の artifact です。必要なものだけ追加してください。バージョンは BOM で揃います。

| モジュール | 用途 |
|---|---|
| `packet` | PacketEvents 経由の偽エンティティ/ブロック |
| `entity` | エンティティのスポーン、AI ゴール、ライフサイクルフック |
| `world` | ブロック/チャンク/プレイヤー編集 DSL |
| `database` | 複数バックエンドの DB アクセスとマイグレーション |
| `i18n` | 多言語メッセージ |
| `util` | キャスト、バージョンチェック、小さなヘルパー |
| `scoreboard-api`, `scoreboard-nms` | スコアボードの完全な API と NMS 実装 |

```kotlin
dependencies {
  implementation(platform("com.peco2282.devcore:devcore-bom:<version>"))
  implementation("com.peco2282.devcore:core:<version>")
  implementation("com.peco2282.devcore:i18n")
}
```
