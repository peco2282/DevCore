package com.peco2282.devcore.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import java.util.concurrent.CompletableFuture

/**
 * Interface for abstracting database access.
 */
interface DatabaseProvider : AutoCloseable {
  /**
   * The JetBrains Exposed [Database] instance.
   */
  val database: Database

  /**
   * The list of tables managed by this provider.
   */
  val tables: List<Table>

  /**
   * Executes a transaction synchronously.
   *
   * @param T The return type of the statement.
   * @param statement The code to execute within the transaction.
   * @return The result of the transaction.
   */
  fun <T> dbQuery(statement: Transaction.() -> T): T

  /**
   * Executes a transaction asynchronously using a [CompletableFuture].
   *
   * @param T The return type of the statement.
   * @param statement The code to execute within the transaction.
   * @return A [CompletableFuture] that will contain the result of the transaction.
   */
  fun <T> dbQueryAsync(statement: Transaction.() -> T): CompletableFuture<T>

  /**
   * Executes a transaction asynchronously using Kotlin coroutines.
   *
   * @param T The return type of the statement.
   * @param statement The code to execute within the transaction.
   * @return The result of the transaction.
   */
  suspend fun <T> dbQuerySuspend(statement: Transaction.() -> T): T
}
