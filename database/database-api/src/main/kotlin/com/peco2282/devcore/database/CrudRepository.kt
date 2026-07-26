package com.peco2282.devcore.database

import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.jdbc.selectAll

/**
 * Abstract repository that provides full CRUD operations on top of [Repository].
 *
 * All blocking methods delegate to [provider.dbQuery]; coroutine variants use
 * [provider.dbQuerySuspend] so callers can choose the concurrency model.
 *
 * Usage:
 * ```kotlin
 * class PlayerRepository(provider: DatabaseProvider) :
 *     CrudRepository<Int, PlayerEntity>(provider) {
 *   override val dao = PlayerEntity
 * }
 * ```
 */
abstract class CrudRepository<ID : Comparable<ID>, E : Entity<ID>>(
  protected val provider: DatabaseProvider
) : Repository<ID, E> {

  override fun findById(id: ID): E? = provider.dbQuery { dao.findById(id) }

  override fun all(): List<E> = provider.dbQuery { dao.all().toList() }

  fun save(init: E.() -> Unit): E = provider.dbQuery { dao.new(init) }

  fun update(id: ID, block: E.() -> Unit): E? = provider.dbQuery {
    dao.findById(id)?.apply(block)
  }

  fun delete(entity: E): Unit = provider.dbQuery { entity.delete() }

  fun deleteById(id: ID): Boolean = provider.dbQuery {
    dao.findById(id)?.also { it.delete() } != null
  }

  fun count(): Long = provider.dbQuery { dao.count() }

  fun exists(id: ID): Boolean = provider.dbQuery { dao.findById(id) != null }

  fun findAll(predicate: () -> Op<Boolean>): List<E> =
    provider.dbQuery { dao.find(predicate).toList() }

  fun page(offset: Long = 0, limit: Int = 20): List<E> = provider.dbQuery {
    dao.wrapRows(dao.table.selectAll().limit(limit).offset(offset)).toList()
  }

  // --- suspend variants ---

  suspend fun saveAsync(init: E.() -> Unit): E = provider.dbQuerySuspend { dao.new(init) }

  suspend fun updateAsync(id: ID, block: E.() -> Unit): E? = provider.dbQuerySuspend {
    dao.findById(id)?.apply(block)
  }

  suspend fun findAllAsync(predicate: () -> Op<Boolean>): List<E> =
    provider.dbQuerySuspend { dao.find(predicate).toList() }

  suspend fun pageAsync(offset: Long = 0, limit: Int = 20): List<E> = provider.dbQuerySuspend {
    dao.wrapRows(dao.table.selectAll().limit(limit).offset(offset)).toList()
  }
}
