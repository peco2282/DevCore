package com.peco2282.devcore.database

import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

/**
 * Interface for DAO-based repositories.
 *
 * @param ID The type of the entity ID.
 * @param E The entity type.
 */
interface Repository<ID : Comparable<ID>, E : Entity<ID>> {
  /**
   * The [org.jetbrains.exposed.v1.dao.EntityClass] for this repository.
   */
  val dao: EntityClass<ID, E>

  /**
   * Finds an entity by its ID.
   *
   * @param id The ID to search for.
   * @return The entity if found, null otherwise.
   */
  fun findById(id: ID): E? = dao.findById(id)

  /**
   * Finds all entities.
   *
   * @return A list of all entities.
   */
  fun all(): List<E> = dao.all().toList()
}