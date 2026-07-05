package com.peco2282.devcore.database.redis

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

/**
 * Interface for Redis database access.
 */
interface RedisProvider : AutoCloseable {
  val pool: JedisPool

  /**
   * Executes an action using a Redis connection.
   *
   * @param T The return type of the action.
   * @param action The action to execute.
   * @return The result of the action.
   */
  fun <T> redis(action: Jedis.() -> T): T = pool.resource.use { it.action() }

  /**
   * Executes a pipeline of commands.
   */
  fun <T> pipeline(action: redis.clients.jedis.Pipeline.() -> T): T = pool.resource.use { jedis ->
    val pipeline = jedis.pipelined()
    val result = pipeline.action()
    pipeline.sync()
    result
  }
}

/**
 * Creates a [RedisProvider].
 *
 * @param action The configuration action for the builder.
 * @return A configured [RedisProvider].
 */
fun createRedis(action: RedisBuilder.() -> Unit): RedisProvider = RedisBuilder().apply(action).build()

/**
 * Builder class for [RedisProvider].
 */
class RedisBuilder {
  var host: String = "localhost"
  var port: Int = 6379
  var password: String? = null
  var timeout: Int = 2000
  var poolConfig: JedisPoolConfig = JedisPoolConfig()

  /**
   * Configures the [JedisPoolConfig].
   */
  fun pool(action: JedisPoolConfig.() -> Unit) {
    poolConfig.action()
  }

  fun build(): RedisProvider {
    val pool = if (password.isNullOrEmpty()) {
      JedisPool(poolConfig, host, port, timeout)
    } else {
      JedisPool(poolConfig, host, port, timeout, password)
    }
    return RedisProviderImpl(pool)
  }
}

internal class RedisProviderImpl(override val pool: JedisPool) : RedisProvider {
  override fun close() {
    pool.close()
  }
}
