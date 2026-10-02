# DevCore Database SQLite

[English] | [[日本語](README.ja.md)]

File-backed SQLite provider using the DevCore JDBC adapter.

```kotlin
dependencies { implementation("com.peco2282.devcore:database-sqlite:<version>") }

val database = createSqlite {
  file = plugin.dataFolder.resolve("data.db")
  table(Users)
}
```
