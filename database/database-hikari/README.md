# DevCore Database Hikari

[English] | [[日本語](README.ja.md)]

HikariCP-backed JDBC and Exposed provider.

```kotlin
dependencies { implementation("com.peco2282.devcore:database-hikari:<version>") }

val database = createHikari {
  config("org.mariadb.jdbc.Driver", "jdbc:mariadb://localhost/app", "user", "password")
  hikari { maximumPoolSize = 10 }
  table(Users)
}
```

Close the returned provider during plugin shutdown to release the connection pool.
