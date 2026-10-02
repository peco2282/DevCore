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
