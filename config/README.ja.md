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
  // implementation(platform("com.peco2282.devcore:devcore-bom:<version>"))
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

Kotlin のプロパティ名と正式な YAML キーが異なる場合は `@ConfigKey` を使用します。
指定したキーは読み込みと書き込みの両方で維持されます。

```kotlin
data class ServiceConfig(
  @ConfigKey("request-timeout")
  val requestTimeout: Int = 30
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

### 複数設定ソースの優先読み込み

`Configs.from` を使用すると、複数のセクションから値を解決できます。
先に渡したセクションほど優先度が高く、キーが存在しない場合は後続の
セクション、最後にコンストラクタの既定値へフォールバックします。
明示的に指定された `0` や `false` は欠落値として扱いません。

```kotlin
val environmentOverrides =
  plugin.config.getConfigurationSection("environments.$environment")
val sharedDefaults = plugin.config.getConfigurationSection("defaults")

val service = Configs.from(environmentOverrides, sharedDefaults)
  .convert<ServiceConfig>()
```

ネストしたセクションも再帰的にマージされ、null のセクションは無視されます。

### 数値の正規化

Clamp アノテーションは、設定の読み込み時に数値を指定範囲へ補正します。
補正後の値で設定オブジェクトを生成し、その値を YAML に書き戻します。

```kotlin
data class LimitsConfig(
  @Clamp(min = 0.0, max = 1.0)
  val ratio: Double = 0.5,

  @ClampAtLeast(1.0)
  val workers: Int = 1,

  @ClampAtMost(60.0)
  val timeout: Long = 30
)
```

Clamp アノテーションは範囲外の値を補正します。一方、`@Range`、`@Min`、
`@Max` などのバリデーションアノテーションは範囲外の値を拒否します。
正規化はバリデーションより先に実行されます。

### 複数プロパティ間の検証

複数のプロパティにまたがる条件には `ValidatableConfig` を実装します。
カスタム検証は各プロパティの検証後に実行され、ネストした設定オブジェクトにも
再帰的に適用されます。

```kotlin
data class IntervalConfig(
  val minimum: Double,
  val maximum: Double
) : ValidatableConfig {
  override fun validate() {
    require(minimum <= maximum) {
      "minimum must not exceed maximum"
    }
  }
}
```

### Bukkit ビルトインシリアライザ

`ItemStack`、`Location`、`Vector` は、手動登録なしで設定データクラスに
直接使用できます。ネストしたオブジェクトやリスト内でも利用できます。

```kotlin
data class ArenaConfig(
  val spawn: Location,
  val direction: Vector,
  val checkpoints: List<Vector>,
  val icon: ItemStack
)
```

Location は `world`、`x`、`y`、`z` と、省略可能な `yaw`、`pitch` を使用します。
Vector は `x`、`y`、`z` を使用します。Bukkit によって復元済みのオブジェクト、
通常の YAML マップ、`ConfigurationSection` のいずれも読み込めます。
存在しないワールドや不足した座標は設定エラーとして報告します。

```yaml
spawn:
  world: world
  x: 10.5
  y: 64.0
  z: -20.5
  yaw: 90.0
  pitch: 0.0
direction:
  x: 1.0
  y: 0.0
  z: -1.0
```

### バリデーションアノテーション

- `@Comment(text)`: YAMLに出力されるコメントを指定します。
- `@ConfigKey(value)`: 読み込みと書き込みで使う正式な YAML キーを指定します。
- `@Clamp(min, max)`: 数値を両端を含む指定範囲へ補正します。
- `@ClampAtLeast(value)`: 数値を指定した最小値以上へ補正します。
- `@ClampAtMost(value)`: 数値を指定した最大値以下へ補正します。
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
- `@Finite`: 浮動小数点数が NaN または無限大でないことを検証します。
- `@URL`: 有効なURL形式であることを検証します。
- `@FileExists`: 指定されたパスのファイルが存在することを検証します。
