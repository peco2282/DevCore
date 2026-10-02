# DevCore Database API

[English] | [[日本語](README.ja.md)]

Shared Exposed-based APIs for DevCore database providers: transactions, repositories, pagination, schema registration, and ordered migrations.

```kotlin
dependencies { implementation("com.peco2282.devcore:database-api:<version>") }
```

Use this artifact when implementing a provider; applications normally depend on a concrete adapter such as `database-hikari` or `database-sqlite`.
