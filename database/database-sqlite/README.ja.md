# DevCore Database SQLite

[[English](README.md)] | [日本語]

DevCore JDBCアダプターを使用するファイルベースのSQLiteプロバイダーです。

```kotlin
dependencies { implementation("com.peco2282.devcore:database-sqlite:<version>") }

val database = createSqlite {
  file = plugin.dataFolder.resolve("data.db")
  table(Users)
}
```
