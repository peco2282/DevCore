# DevCore Config
[[English](README.md)] | [日本語]

`config.yml` 等の YAML を Kotlin のデータクラスへマッピングし、バリデーションを行うモジュールです。

## 特徴

- Kotlinのデータクラスによる型安全な設定管理
- YAMLファイルへのコメントの自動挿入
- アノテーションベースのバリデーション
- ネストされたクラス、リスト、マップのサポート
- 自動的なロード/セーブ/再読み込み

## Install (Gradle Kotlin DSL)
```kotlin
dependencies {
  implementation("com.peco2282.devcore:config:<version>")
  // または:
  // implementation(platform("com.peco2282.devcore:bom:<version>"))
  // implementation("com.peco2282.devcore:config")
}
```

## 使用方法

### 設定クラスの定義

```kotlin
@Comment("プラグインのメイン設定")
data class MyConfig(
  @Comment("プレイヤーの名前")
  @NotBlank
  val name: String = "Steve",

  @Comment("レベル (1-100)")
  @Range(min = 1, max = 100)
  val level: Int = 1,

  @Comment("有効かどうか")
  val enabled: Boolean = true
)
```

### 設定のロードとセーブ

```kotlin
// ロード (config.yml)
val config = Configs.load<MyConfig>(plugin)

// 特定のファイルからロード
val otherConfig = Configs.load<OtherConfig>(File(plugin.dataFolder, "other.yml"))

// セーブ
Configs.save(plugin, config)
```

### バリデーションアノテーション

- `@Comment(text)`: YAMLに出力されるコメントを指定します。
- `@NotBlank`: 文字列が空または空白でないことを検証します。
- `@NotEmpty`: 文字列、コレクション、マップが空でないことを検証します。
- `@Range(min, max)`: 数値が指定範囲内であることを検証します。
- `@Size(min, max)`: コレクションの要素数が範囲内であることを検証します。
- `@Regex(pattern)`: 文字列が正規表現にマッチするか検証します。
- `@Email`: 文字列がメールアドレス形式であることを検証します。
- `@Min(value)`, `@Max(value)`: 数値の最小値、最大値を指定します。
- `@Positive`: 数値が正（0より大きい）であることを検証します。
- `@Negative`: 数値が負（0未満）であることを検証します。
- `@NonNegative`: 数値が0以上であることを検証します。
- `@URL`: 有効なURL形式であることを検証します。
- `@FileExists`: 指定されたパスのファイルが存在することを検証します。
