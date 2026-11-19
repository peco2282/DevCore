package com.peco2282.devcore.database.sqlite

import com.peco2282.devcore.database.MigrationRunner
import com.peco2282.devcore.database.migration
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private object Items : Table("items") {
  val name = varchar("name", 64)
}

class SqliteMigrationTest {
  private lateinit var dir: File

  @BeforeTest
  fun setUp() { dir = Files.createTempDirectory("sqlite-test").toFile() }

  @AfterTest
  fun tearDown() { dir.deleteRecursively() }

  private fun provider() = createSqlite { file = File(dir, "test.db") }

  @Test
  fun `dbQuery round trips data`() {
    provider().use { db ->
      db.dbQuery {
        SchemaUtils.create(Items)
        Items.insert { it[name] = "sword" }
      }
      assertEquals(listOf("sword"), db.dbQuery { Items.selectAll().map { it[Items.name] } })
    }
  }

  @Test
  fun `migrations run in version order and only once`() {
    val order = mutableListOf<Int>()
    val migrations = listOf(
      migration(2) { order += 2 },
      migration(1) { order += 1 }
    )
    provider().use { db ->
      MigrationRunner(db, migrations).run()
      MigrationRunner(db, migrations).run()
    }
    assertEquals(listOf(1, 2), order)
  }

  @Test
  fun `only newly added migrations run on later startup`() {
    val order = mutableListOf<Int>()
    provider().use { db ->
      MigrationRunner(db, listOf(migration(1) { order += 1 })).run()
      MigrationRunner(db, listOf(migration(1) { order += 1 }, migration(2) { order += 2 })).run()
    }
    assertEquals(listOf(1, 2), order)
  }

  @Test
  fun `failed migration is not recorded and rolls back`() {
    provider().use { db ->
      val bad = migration(1) {
        SchemaUtils.create(Items)
        error("boom")
      }
      assertFailsWith<IllegalStateException> { MigrationRunner(db, listOf(bad)).run() }

      val order = mutableListOf<Int>()
      MigrationRunner(db, listOf(migration(1) { order += 1 })).run()
      assertEquals(listOf(1), order)
    }
  }
}
