package com.peco2282.devcore.database.hikari

import com.peco2282.devcore.database.DatabaseBuilder
import com.peco2282.devcore.database.DatabaseProvider
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.concurrent.CompletableFuture

/**
 * Creates a [DatabaseProvider] using HikariCP.
 *
 * @param builder The configuration builder for the database.
 * @return A configured [DatabaseProvider] instance.
 * @throws IllegalStateException If the database configuration is not specified.
 */
fun createHikari(builder: HikariDatabaseBuilder.() -> Unit): DatabaseProvider =
  HikariDatabaseBuilder().apply(builder).build()

/**
 * Implementation of [DatabaseBuilder] for HikariCP connections.
 */
class HikariDatabaseBuilder : DatabaseBuilder() {
  private val hikariConfig = HikariConfig()

  /**
   * Configures the [HikariConfig] directly.
   *
   * @param action The configuration action.
   */
  fun hikari(action: HikariConfig.() -> Unit) {
    hikariConfig.action()
  }

  override fun build(): DatabaseProvider {
    val cfg = config ?: throw IllegalStateException("Database config is not specified")

    hikariConfig.jdbcUrl = cfg.url
    hikariConfig.driverClassName = cfg.driver
    hikariConfig.username = cfg.user
    hikariConfig.password = cfg.password

    val dataSource = HikariDataSource(hikariConfig)
    val db = Database.connect(dataSource)

    // Table initialization
    transaction(db) {
      SchemaUtils.create(*tables.toTypedArray())
    }

    return HikariDatabaseProviderImpl(db, dataSource, tables.toList())
  }
}

/**
 * HikariCP implementation of [DatabaseProvider].
 *
 * @property database The JetBrains Exposed [Database] instance.
 * @property dataSource The [HikariDataSource] instance.
 * @property tables The list of tables managed by this provider.
 */
internal class HikariDatabaseProviderImpl(
  override val database: Database,
  private val dataSource: HikariDataSource,
  override val tables: List<Table>
) : DatabaseProvider, AutoCloseable {

  override fun <T> dbQuery(statement: Transaction.() -> T): T =
    transaction(db = database, statement = statement)

  override fun <T> dbQueryAsync(statement: Transaction.() -> T): CompletableFuture<T> =
    CompletableFuture.supplyAsync {
      dbQuery(statement)
    }

  override suspend fun <T> dbQuerySuspend(statement: Transaction.() -> T): T =
    withContext(Dispatchers.IO) {
      dbQuery(statement)
    }

  override fun close() {
    dataSource.close()
  }
}
