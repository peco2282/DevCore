package com.peco2282.devcore.database

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.Boolean

// ---------------------------------------------------------------------------
// Paginated query result
// ---------------------------------------------------------------------------

/**
 * A slice of query results with pagination metadata.
 */
data class PageResult<T>(
  val items: List<T>,
  val total: Long,
  val offset: Long,
  val limit: Int
) {
  val hasNext: Boolean get() = offset + limit < total
  val hasPrev: Boolean get() = offset > 0
  val currentPage: Int get() = if (limit > 0) (offset / limit).toInt() else 0
}

/**
 * Executes a paginated SELECT against this table inside a [provider] transaction.
 *
 * ```kotlin
 * val page = PlayersTable.selectPage(
 *   provider = db,
 *   offset = 0,
 *   limit = 10,
 *   where = { PlayersTable.score greaterThan 100 },
 *   orderBy = listOf(PlayersTable.score to SortOrder.DESC)
 * ) { PlayersTable.mapRow(this) }
 * ```
 */
fun <T : Table, R> T.selectPage(
  provider: DatabaseProvider,
  offset: Long = 0,
  limit: Int = 20,
  where: (() -> Op<Boolean>)? = null,
  orderBy: List<Pair<Expression<*>, SortOrder>> = emptyList(),
  mapper: ResultRow.(T) -> R
): PageResult<R> = provider.dbQuery {
  val baseQuery = selectAll()
  val query = if (where != null) baseQuery.where(where) else baseQuery

  val total = query.count()

  val items = query
    .also { q -> if (orderBy.isNotEmpty()) q.orderBy(*orderBy.toTypedArray()) }
    .limit(limit)
    .offset(offset)
    .map { row -> row.mapper(this@selectPage) }

  PageResult(items, total, offset, limit)
}

// ---------------------------------------------------------------------------
// Predicate composition
// ---------------------------------------------------------------------------

/**
 * Combines two Exposed where-clause predicates with AND.
 *
 * ```kotlin
 * val spec = nameSpec and activeSpec
 * MyTable.selectAll().where(spec)
 * ```
 */
infix fun (() -> Op<Boolean>).and(other: () -> Op<Boolean>): () -> Op<Boolean> {
  val left = this
  return { left() and other() }
}

/**
 * Combines two Exposed where-clause predicates with OR.
 */
infix fun (() -> Op<Boolean>).or(other: () -> Op<Boolean>): () -> Op<Boolean> {
  val left = this
  return { left() or other() }
}

infix operator fun (() -> Op<Boolean>).plus(other: () -> Op<Boolean>): (() -> Op<Boolean>) = and(other)

