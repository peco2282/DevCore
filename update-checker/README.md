# update-checker

[English] | [日本語](README.ja.md)

An opt-in, asynchronous update check using only JDK `HttpClient`.

```kotlin
plugin.checkForUpdates(
  source = Github("peco2282/MyPlugin"),
  enabled = config.getBoolean("update-checker.enabled", false)
) { result ->
  if (result.isNewer) logger.info("${result.latest}: ${result.url}")
}
```

No request is made unless `enabled` is true. The check runs once per call and never blocks plugin startup. HTTP, parsing and network failures complete with `null` and are logged at `FINE`. Keep the returned `CompletableFuture` when callers need completion tracking. Custom services can implement `UpdateSource`.
