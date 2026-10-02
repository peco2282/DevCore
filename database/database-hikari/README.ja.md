# DevCore Database Hikari

[[English](README.md)] | [日本語]

HikariCPベースのJDBC・Exposedプロバイダーです。

```kotlin
dependencies { implementation("com.peco2282.devcore:database-hikari:<version>") }

val database = createHikari {
  config("org.mariadb.jdbc.Driver", "jdbc:mariadb://localhost/app", "user", "password")
  hikari { maximumPoolSize = 10 }
  table(Users)
}
```

接続プールを解放するため、プラグイン停止時に返されたプロバイダーを閉じてください。
