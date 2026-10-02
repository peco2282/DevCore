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
