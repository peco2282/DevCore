# DevCore Database MySQL

[[English](README.md)] | [日本語]

DevCore JDBCプロバイダーを使用するMySQL用ビルダーです。

```kotlin
dependencies { implementation("com.peco2282.devcore:database-mysql:<version>") }

val database = createMySql {
  host = "localhost"
  database = "minecraft"
  config = DatabaseConfig("", "", "user", "password")
  table(Users)
}
```
