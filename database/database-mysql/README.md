# DevCore Database MySQL

[English] | [[日本語](README.ja.md)]

Convenience builder for MySQL using the DevCore JDBC provider.

```kotlin
dependencies { implementation("com.peco2282.devcore:database-mysql:<version>") }

val database = createMySql {
  host = "localhost"
  database = "minecraft"
  config = DatabaseConfig("", "", "user", "password")
  table(Users)
}
```
