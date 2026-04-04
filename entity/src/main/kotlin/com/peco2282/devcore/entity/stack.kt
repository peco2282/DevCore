package com.peco2282.devcore.entity

import org.bukkit.entity.Entity

/**
 * Builder for creating entity stacks (passenger chains) using a DSL approach.
 * 
 * This class provides a type-safe way to build chains of entities where each subsequent
 * entity becomes a passenger of the previous one. Entities are added using the unary plus
 * operator (+) within the DSL block.
 * 
 * @see mountStack
 */
@EntityDsl
class EntityStackBuilder {
  private val stack = mutableListOf<Entity>()

  /**
   * Adds an entity to the stack using the unary plus operator.
   * 
   * Usage example:
   * ```kotlin
   * entity.mountStack {
   *   +passenger1
   *   +passenger2
   * }
   * ```
   */
  operator fun Entity.unaryPlus() {
    stack.add(this)
  }

  /**
   * Builds and returns the list of entities in the stack.
   * 
   * @return immutable list of entities that were added to the stack
   */
  fun build(): List<Entity> = stack
}

/**
 * Builds an entity stack (passengers) where each entity mounts on top of the previous one.
 * 
 * Creates a chain of passengers starting from this entity. Each entity in the DSL block
 * becomes a passenger of the previous entity, forming a vertical stack.
 * 
 * Example usage:
 * ```kotlin
 * baseEntity.mountStack {
 *   +armorStand1  // mounts on baseEntity
 *   +armorStand2  // mounts on armorStand1
 *   +displayEntity // mounts on armorStand2
 * }
 * ```
 * 
 * @param block the DSL block to add entities to the stack using the unary plus operator
 */
fun Entity.mountStack(block: EntityStackBuilder.() -> Unit) {
  val entities = EntityStackBuilder().apply(block).build()
  var current = this
  entities.forEach { entity ->
    current.addPassenger(entity)
    current = entity
  }
}
