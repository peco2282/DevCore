package com.peco2282.devcore.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll

internal object MigrationTable : Table("devcore_migrations") {
  val version = integer("version").uniqueIndex()
  val description = varchar("description", 255)
  val appliedAt = long("applied_at")
  override val primaryKey = PrimaryKey(version)
}

/**
 * Applies pending [migrations] against the given [provider].
 *
 * On first use, creates the `devcore_migrations` tracking table automatically.
 * Each migration runs in its own transaction and is recorded immediately after.
 * Migrations are applied in ascending [Migration.version] order.
 */
class MigrationRunner(
  private val provider: DatabaseProvider,
  private val migrations: List<Migration>
) {
  fun run() {
    if (migrations.isEmpty()) return

    provider.dbQuery {
      SchemaUtils.create(MigrationTable)
    }

    val applied = provider.dbQuery {
      MigrationTable.selectAll().map { it[MigrationTable.version] }.toSet()
    }

    migrations
      .sortedBy { it.version }
      .filter { it.version !in applied }
      .forEach { migration ->
        provider.dbQuery {
          with(migration) { migrate() }
          MigrationTable.insert {
            it[version] = migration.version
            it[description] = migration.description
            it[appliedAt] = System.currentTimeMillis()
          }
        }
      }
  }
}
