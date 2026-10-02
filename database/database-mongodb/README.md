# DevCore Database MongoDB

[English] | [[日本語](README.ja.md)]

MongoDB client and collection DSL.

```kotlin
dependencies { implementation("com.peco2282.devcore:database-mongodb:<version>") }

val mongo = createMongo {
  connectionString = "mongodb://localhost:27017"
  databaseName = "minecraft"
}
val players = mongo.getCollection("players")
```

Close the provider during plugin shutdown.
