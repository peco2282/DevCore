# DevCore Database

[[English](README.md)] | [日本語]

DevCoreのデータベースAPIとアダプターです。利用するストレージに対応するアーティファクトを選択してください。

| アーティファクト | 用途 |
| --- | --- |
| `database-api` | Exposedトランザクション、リポジトリ、ページネーション、マイグレーション |
| `database-jdbc` | 汎用JDBCプロバイダー |
| `database-hikari` | HikariCPベースのJDBCプロバイダー |
| `database-mysql` | MySQL用ビルダー |
| `database-sqlite` | SQLite用ビルダー |
| `database-redis` | Jedis接続プールDSL |
| `database-mongodb` | MongoDBクライアントDSL |

[DevCore BOM](../bom/README.ja.md)を使うと個々の依存関係からバージョンを省略できます。
