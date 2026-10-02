# DevCore Database MongoDB

[[English](README.md)] | [日本語]

MongoDBクライアントとコレクションを扱うDSLです。

```kotlin
dependencies { implementation("com.peco2282.devcore:database-mongodb:<version>") }

val mongo = createMongo {
  connectionString = "mongodb://localhost:27017"
  databaseName = "minecraft"
}
val players = mongo.getCollection("players")
```

プラグイン停止時にプロバイダーを閉じてください。
