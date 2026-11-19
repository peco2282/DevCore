# DevCore I18N

[[English](README.md)] | [日本語]

プレイヤーのロケール、フォールバック、再読み込み、`MessageFormat`形式の引数に対応するBukkitプラグイン向けYAML国際化機能です。

## 導入方法

```kotlin
dependencies { implementation("com.peco2282.devcore:i18n:<version>") }
```

## 使用例

`plugins/<plugin>/i18n/ja_JP.yml`を作成します。

```yaml
welcome: "ようこそ、{0}さん！"
```

```kotlin
val i18n = I18NManager.get(plugin)
player.sendMessage(i18n.translate(player, "welcome", player.name) ?: "ようこそ！")
```

### MiniMessage によるリッチテキスト

`component` はメッセージを MiniMessage として解析し、プレースホルダは単なるテキストとして挿入します。
プレイヤー由来の値(名前やチャット)で装飾を注入されることはありません。キーが無い場合は、黙って失敗せずキー名を表示します。

```yaml
greet: "<green>こんにちは、<name>さん！ アイテムが <count> 個あります。"
```

```kotlin
i18n.send(player, "greet", "name" to player.name, "count" to 3)
// または: player.sendMessage(i18n.component(player, "greet", "name" to player.name, "count" to 3))
```

### フォールバック順

ロケールが `ja_JP` のプレイヤーでは、`ja_JP` → `ja` → デフォルトロケール(`i18n.defaultLocale`、既定は `en_US`)→ その言語、の順に探します。
言語ファイルがあってもキーが無い場合は、デフォルトにフォールバックします。

