# DevCore I18N

[English] | [[日本語](README.ja.md)]

YAML-based localization for Bukkit plugins with player locales, fallback, reloads, and `MessageFormat` placeholders.

## Install

```kotlin
dependencies { implementation("com.peco2282.devcore:i18n:<version>") }
```

## Example

Create `plugins/<plugin>/i18n/en_US.yml`:

```yaml
welcome: "Welcome, {0}!"
```

```kotlin
val i18n = I18NManager.get(plugin)
player.sendMessage(i18n.translate(player, "welcome", player.name) ?: "Welcome!")
```

### Rich text with MiniMessage

`component` parses the message with MiniMessage and inserts placeholders as plain text, so player-supplied
values (names, chat) cannot inject formatting. A missing key shows the key itself instead of failing silently.

```yaml
greet: "<green>Hello, <name>! You have <count> items."
```

```kotlin
i18n.send(player, "greet", "name" to player.name, "count" to 3)
// or: player.sendMessage(i18n.component(player, "greet", "name" to player.name, "count" to 3))
```

### Fallback order

For a player with locale `ja_JP`, a key is looked up in `ja_JP`, then `ja`, then the default locale
(`i18n.defaultLocale`, `en_US` unless changed), then its language. A locale file that exists but lacks
the key therefore still falls back to the default.

