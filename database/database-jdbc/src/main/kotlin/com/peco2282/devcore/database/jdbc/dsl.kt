package com.peco2282.devcore.database.jdbc

import com.peco2282.devcore.database.DatabaseBuilder
import com.peco2282.devcore.database.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.concurrent.CompletableFuture

/**
 * Creates a [DatabaseProvider] using JDBC.
 *
 * @param builder The configuration builder for the database.
 * @return A configured [DatabaseProvider] instance.
 * @throws IllegalStateException If the database configuration is not specified.
 */
fun createJdbc(builder: DatabaseBuilder.() -> Unit): DatabaseProvider = DatabaseBuilderImpl().apply(builder).build()

/**
 * Implementation of [DatabaseBuilder] for JDBC connections.
 */
internal class DatabaseBuilderImpl : DatabaseBuilder() {
  /**
   * Builds a [DatabaseProvider] with a JDBC connection.
   * Connects to the database and initializes the registered tables.
   *
   * @return A [DatabaseProvider] instance.
   * @throws IllegalStateException If the database configuration is not specified.
   */
  override fun build(): DatabaseProvider {
    val cfg = config ?: throw IllegalStateException("Database config is not specified")
    val db = Database.connect(
      url = cfg.url,
      driver = cfg.driver,
      user = cfg.user,
      password = cfg.password
    )

    // Table initialization
    transaction(db) {
      SchemaUtils.create(*tables.toTypedArray())
    }

    return DatabaseProviderImpl(db)
  }
}

/**
 * Default implementation of [DatabaseProvider].
 *
 * @property database The JetBrains Exposed [Database] instance.
 */
internal class DatabaseProviderImpl(val database: Database) : DatabaseProvider {
  /**
   * Executes a transaction synchronously.
   *
   * @param T The return type of the statement.
   * @param statement The code to execute within the transaction.
   * @return The result of the transaction.
   */
  override fun <T> dbQuery(statement: Transaction.() -> T): T = transaction(database, statement = statement)

  /**
   * Executes a transaction asynchronously using a [CompletableFuture].
   *
   * @param T The return type of the statement.
   * @param statement The code to execute within the transaction.
   * @return A [CompletableFuture] that will contain the result of the transaction.
   */
  override fun <T> dbQueryAsync(statement: Transaction.() -> T): CompletableFuture<T> = CompletableFuture.supplyAsync {
    dbQuery(statement)
  }

  /**
   * Executes a transaction asynchronously using Kotlin coroutines.
   * This implementation uses [Dispatchers.IO] for non-blocking I/O.
   *
   * @param T The return type of the statement.
   * @param statement The code to execute within the transaction.
   * @return The result of the transaction.
   */
  override suspend fun <T> dbQuerySuspend(statement: Transaction.() -> T): T = withContext(Dispatchers.IO) {
    dbQuery(statement)
  }
}
