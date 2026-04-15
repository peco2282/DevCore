# util

General utility module providing coroutine support, version checking, and common helpers.

## Usage

### Coroutines
Provides simplified access to Bukkit-threaded coroutines.

```kotlin
plugin.launch {
    // Executes on the Bukkit primary thread by default
    val data = fetchDataAsync() // suspend function
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
- **Bukkit Coroutine Dispatcher**: Seamless integration with Kotlin Coroutines on the Minecraft main thread.
- **Version Management**: Robust version comparison to handle multi-version compatibility.
- **Internal API Tools**: Dedicated annotations for internal-only features.
- **Error Handling Utilities**: Functional interfaces for safer execution.
