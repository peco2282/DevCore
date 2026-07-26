package com.peco2282.devcore.database

import org.jetbrains.exposed.v1.core.Transaction

/**
 * Represents a single schema migration step.
 *
 * Migrations are applied in [version] order and tracked in a `devcore_migrations` table
 * so each migration runs exactly once.
 */
@DatabaseDsl
interface Migration {
  val version: Int
  val description: String get() = "Migration v$version"
  fun Transaction.migrate()
}

/**
 * Creates a [Migration] inline via a DSL block.
 *
 * ```kotlin
 * migrate(
 *   migration(1, "add email column") {
 *     exec("ALTER TABLE users ADD COLUMN email VARCHAR(255)")
 *   }
 * )
 * ```
 */
fun migration(
  version: Int,
  description: String = "Migration v$version",
  block: Transaction.() -> Unit
): Migration = object : Migration {
  override val version = version
  override val description = description
  override fun Transaction.migrate() = block()
}
