# DevCore Database JDBC

[[English](README.md)] | [日本語]

汎用JDBC・Exposedプロバイダーです。

```kotlin
dependencies { implementation("com.peco2282.devcore:database-jdbc:<version>") }

val database = createJdbc {
  config("org.example.Driver", "jdbc:example://localhost/app", "user", "password")
  table(Users)
  autoMigrate = true
}
```

JDBCドライバーは実行時に別途必要です。プラグイン停止時に返されたプロバイダーを閉じてください。
