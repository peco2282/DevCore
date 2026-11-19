# anvil-input

[English] | [日本語](README.ja.md)

Collect a short player string through the anvil rename field.

```kotlin
player.askText(
  plugin,
  title = Component.text("Choose a name"),
  initial = player.name
) { input ->
  player.displayName(Component.text(input))
}
```

The callback runs synchronously after a non-blank result-slot click. Closing the inventory cancels the session without calling it; opening another prompt replaces the player's previous session. Call `plugin.anvilInputs().close()` during manual teardown when the plugin lifecycle is managed outside normal disable handling.

The rename API moved in Paper. Include the matching runtime adapter:

```kotlin
runtimeOnly("com.peco2282.devcore:v1_20_6:<version>") // Paper 1.20.6
// or v1_21_4 for Paper 1.21.x
```
