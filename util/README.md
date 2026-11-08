# util

General utility module providing version checking, safe casting, and common helpers.

## Usage

### Coroutines
> **Deprecated.** `Plugin.launch`, `Plugin.scope` and `Plugin.coroutine` in `util` are deprecated.
> Use the `scheduler` module instead (`com.peco2282.devcore.scheduler.coroutines`): its scope is
> cancelled automatically when the plugin is disabled, which the `util` version is not.

```kotlin
plugin.scheduler.launch {
    val data = fetchData()      // suspend function
    player.sendMessage("Data fetched!")
}
```

### Version Checking
Easily check if the current server version meets specific requirements.

```kotlin
checkVersion("1.21.1") // Throws UnsupportedVersionException if below
```

### Casting Helpers
Safe and convenient casting with optional error handling.

```kotlin
val player = sender.asNullable<Player>()
val entity = something.asNotNullable<LivingEntity>()
```

## Features
- **Version Management**: Robust version comparison to handle multi-version compatibility.
- **Internal API Tools**: Dedicated annotations for internal-only features.
- **Error Handling Utilities**: Functional interfaces for safer execution.
