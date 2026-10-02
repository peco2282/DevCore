# DevCore Database Redis

[English] | [[日本語](README.ja.md)]

Jedis connection-pool DSL for Redis commands and pipelines.

```kotlin
dependencies { implementation("com.peco2282.devcore:database-redis:<version>") }

val redis = createRedis { host = "localhost" }
redis.redis { set("server:status", "online") }
```

Close the provider during plugin shutdown.
