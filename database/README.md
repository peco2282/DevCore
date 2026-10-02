# DevCore Database

[English] | [[日本語](README.ja.md)]

Database APIs and adapters for DevCore. Choose the artifact that matches your storage backend.

| Artifact | Purpose |
| --- | --- |
| `database-api` | Exposed transactions, repositories, pagination, and migrations |
| `database-jdbc` | Generic JDBC provider |
| `database-hikari` | JDBC provider backed by HikariCP |
| `database-mysql` | MySQL convenience builder |
| `database-sqlite` | SQLite convenience builder |
| `database-redis` | Jedis pool DSL |
| `database-mongodb` | MongoDB client DSL |

Use the [DevCore BOM](../bom/README.md) to omit versions from the individual dependencies.
