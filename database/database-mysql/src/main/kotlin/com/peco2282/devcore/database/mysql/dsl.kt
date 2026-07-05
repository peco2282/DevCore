package com.peco2282.devcore.database.mysql

import com.peco2282.devcore.database.DatabaseBuilder
import com.peco2282.devcore.database.DatabaseProvider
import com.peco2282.devcore.database.jdbc.createJdbc

/**
 * Creates a [DatabaseProvider] for MySQL.
 *
 * @param builder The configuration builder for the database.
 * @return A configured [DatabaseProvider] instance.
 */
fun createMySql(builder: MySqlDatabaseBuilder.() -> Unit): DatabaseProvider =
  MySqlDatabaseBuilder().apply(builder).build()

/**
 * Builder class for MySQL database connections.
 */
class MySqlDatabaseBuilder : DatabaseBuilder() {
  var host: String = "localhost"
  var port: Int = 3306
  var database: String = ""
  var parameters: Map<String, String> = emptyMap()

  override fun build(): DatabaseProvider {
    val params = if (parameters.isNotEmpty()) {
      "?" + parameters.entries.joinToString("&") { "${it.key}=${it.value}" }
    } else {
      ""
    }

    val jdbcUrl = "jdbc:mysql://$host:$port/$database$params"

    return createJdbc {
      config("com.mysql.cj.jdbc.Driver", jdbcUrl, config?.user ?: "", config?.password ?: "")
      this@MySqlDatabaseBuilder.tables.forEach { table(it) }
    }
  }
}
