# DevCore Database JDBC

[English] | [[日本語](README.ja.md)]

Generic JDBC and Exposed provider.

```kotlin
dependencies { implementation("com.peco2282.devcore:database-jdbc:<version>") }

val database = createJdbc {
  config("org.example.Driver", "jdbc:example://localhost/app", "user", "password")
  table(Users)
  autoMigrate = true
}
```

The JDBC driver itself must be available at runtime. Close the returned provider during plugin shutdown.
