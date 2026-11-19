# debug

[English] | [日本語](README.ja.md)

Category-based debug logging with explicit runtime enablement and timing helpers.

```kotlin
val debug = plugin.debugController()
debug.enable("storage")
val log = plugin.debugLogger("storage")
log.debug { "Loading ${player.uniqueId}" }
val result = log.timed("load player") { repository.load(player.uniqueId) }
debug.disable("storage")
```

Messages are built lazily and are not sent while a category is disabled. `timed` logs elapsed time and returns the block result; exceptions are rethrown.
