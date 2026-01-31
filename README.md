# DevCore

Core library for the DevCore project.

## Requirements
- JDK 17+

## Build
```bash
./gradlew build
```

## Publish
Set either Gradle properties or environment variables, then run `./gradlew publish`.

- Gradle properties: `devcore.publish.releaseUrl`, `devcore.publish.snapshotUrl`, `devcore.publish.user`, `devcore.publish.password`
- Environment variables: `DEVCORE_PUBLISH_RELEASE_URL`, `DEVCORE_PUBLISH_SNAPSHOT_URL`, `DEVCORE_PUBLISH_USER`, `DEVCORE_PUBLISH_PASSWORD`

## Usage (Gradle)
Consume the unified dependency:

```kotlin
dependencies {
  implementation("com.peco2282.devcore:core:<version>")
}
```

Or use the BOM and pick modules:

```kotlin
dependencies {
  implementation(platform("com.peco2282.devcore:bom:<version>"))
  implementation("com.peco2282.devcore:command")
  implementation("com.peco2282.devcore:config")
}
```

## Modules
- [`core`](core/README.md): Umbrella artifact (pulls in all modules)
- [`bom`](bom/README.md): BOM for aligning module versions
- [`adventure`](adventure/README.md): Adventure `Component` / `Style` DSL helpers
- [`command`](command/README.md): Paper Brigadier command DSL
- [`config`](config/README.md): YAML config mapping + validation
- [`scheduler`](scheduler/README.md): Bukkit scheduler DSL + coroutine dispatcher
- [`cooldown`](cooldown/README.md): Cooldown / debounce utilities

## License
Apache License 2.0. See `LICENSE`.
