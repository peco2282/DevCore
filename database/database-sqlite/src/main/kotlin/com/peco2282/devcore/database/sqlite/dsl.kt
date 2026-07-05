package com.peco2282.devcore.database.sqlite

import com.peco2282.devcore.database.DatabaseBuilder
import com.peco2282.devcore.database.DatabaseProvider
import com.peco2282.devcore.database.jdbc.createJdbc
import java.io.File

/**
 * Creates a [DatabaseProvider] for SQLite.
 *
 * @param builder The configuration builder for the database.
 * @return A configured [DatabaseProvider] instance.
 */
fun createSqlite(builder: SqliteDatabaseBuilder.() -> Unit): DatabaseProvider =
  SqliteDatabaseBuilder().apply(builder).build()

/**
 * Builder class for SQLite database connections.
 */
class SqliteDatabaseBuilder : DatabaseBuilder() {
  var path: String = "database.db"
  var file: File? = null
    set(value) {
      field = value
      if (value != null) {
        path = value.absolutePath
      }
    }

  override fun build(): DatabaseProvider {
    val jdbcUrl = "jdbc:sqlite:$path"

    return createJdbc {
      config("org.sqlite.JDBC", jdbcUrl)
      this@SqliteDatabaseBuilder.tables.forEach { table(it) }
    }
  }
}
