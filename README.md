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

## License
Apache License 2.0. See `LICENSE`.
