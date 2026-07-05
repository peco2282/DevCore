package com.peco2282.devcore.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

/**
 * Marker annotation for Database DSL.
 */
@DslMarker
annotation class DatabaseDsl

/**
 * Executes a transaction synchronously.
 */
fun <T> dbQuery(database: Database? = null, statement: Transaction.() -> T): T =
  transaction(db = database, statement = statement)

/**
 * Executes a transaction asynchronously using Kotlin coroutines.
 */
suspend fun <T> dbQuerySuspend(database: Database? = null, statement: suspend Transaction.() -> T): T =
  newSuspendedTransaction(Dispatchers.IO, db = database, statement = statement)

/**
 * Extension function for [Table] to execute a query within its context.
 */
fun <T : Table, R> T.query(provider: DatabaseProvider, statement: Transaction.(T) -> R): R =
  provider.dbQuery { statement(this@query) }

/**
 * Extension function for [Table] to execute an asynchronous query within its context.
 */
suspend fun <T : Table, R> T.querySuspend(provider: DatabaseProvider, statement: Transaction.(T) -> R): R =
  provider.dbQuerySuspend { statement(this@querySuspend) }

/**
 * Executes a statement for each table registered in the provider.
 */
fun DatabaseProvider.forEachTable(statement: Transaction.(Table) -> Unit) = dbQuery {
  tables.forEach { statement(it) }
}

/**
 * Data class representing the configuration for a database connection.
 *
 * @property driver The JDBC driver class name.
 * @property url The JDBC connection URL.
 * @property user The username for the database connection.
 * @property password The password for the database connection.
 */
data class DatabaseConfig(
  val driver: String,
  val url: String,
  val user: String = "",
  val password: String = ""
)

/**
 * Abstract builder class for constructing a [DatabaseProvider].
 */
@DatabaseDsl
abstract class DatabaseBuilder {
  /**
   * The database configuration.
   */
  var config: DatabaseConfig? = null

  /**
   * The list of tables to be managed by the database.
   */
  protected val tables = mutableListOf<Table>()

  /**
   * Sets the database configuration.
   *
   * @param config The [DatabaseConfig] object.
   */
  fun config(config: DatabaseConfig) {
    this.config = config
  }

  /**
   * Sets the database configuration using individual parameters.
   *
   * @param driver The JDBC driver class name.
   * @param url The JDBC connection URL.
   * @param user The username for the database connection.
   * @param password The password for the database connection.
   */
  fun config(driver: String, url: String, user: String = "", password: String = "") {
    this.config = DatabaseConfig(driver, url, user, password)
  }

  /**
   * Registers one or more tables to the database.
   * These tables will be created or updated upon initialization.
   *
   * @param table The tables to register.
   */
  fun table(vararg table: Table) {
    tables.addAll(table)
  }

  /**
   * Builds and returns a [DatabaseProvider] instance.
   *
   * @return A [DatabaseProvider] configured with the specified parameters.
   * @throws IllegalStateException If the database configuration is not specified.
   */
  abstract fun build(): DatabaseProvider

  /**
   * Registers a table to the database.
   *
   * @param T The table type.
   * @param table The table instance.
   * @return The registered table instance.
   */
  fun <T : Table> register(table: T): T {
    tables.add(table)
    return table
  }
}
