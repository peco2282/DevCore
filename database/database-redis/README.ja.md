# DevCore Database Redis

[[English](README.md)] | [日本語]

Redisコマンドとパイプラインを扱うJedis接続プールDSLです。

```kotlin
dependencies { implementation("com.peco2282.devcore:database-redis:<version>") }

val redis = createRedis { host = "localhost" }
redis.redis { set("server:status", "online") }
```

プラグイン停止時にプロバイダーを閉じてください。
