package com.peco2282.devcore.entity

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType


/**
 * Spawns an entity of the specified type at the given location in this world.
 *
 * @param entity The type of entity to spawn
 * @param location The location where the entity should be spawned
 * @param editor Optional lambda to configure the entity after spawning
 * @return The spawned entity
 */
fun World.spawn(entity: EntityType, location: Location, editor: Entity.() -> Unit = {}): Entity =
  spawnEntity(location, entity).apply(editor)

/**
 * Spawns an entity of type [E] at the given location in this world.
 *
 * @param E The type of entity to spawn
 * @param location The location where the entity should be spawned
 * @param editor Optional lambda to configure the entity after spawning
 * @return The spawned entity of type [E]
 */
inline fun <reified E : Entity> World.spawn(location: Location, noinline editor: E.() -> Unit = {}): E =
  spawn(location, E::class.java, editor)

/**
 * Spawns an entity of the specified type at this location.
 *
 * @param entity The type of entity to spawn
 * @param editor Optional lambda to configure the entity after spawning
 * @return The spawned entity
 */
fun Location.spawn(entity: EntityType, editor: Entity.() -> Unit = {}): Entity =
  world.spawn(entity, this, editor)

/**
 * Spawns an entity of type [E] at this location.
 *
 * @param E The type of entity to spawn
 * @param editor Optional lambda to configure the entity after spawning
 * @return The spawned entity of type [E]
 */
inline fun <reified E : Entity> Location.spawn(noinline editor: E.() -> Unit = {}): E =
  world.spawn(this, E::class.java, editor)

/**
 * Gets all nearby entities of type [E] within the specified radius.
 *
 * @param E The type of entities to retrieve
 * @param radius The radius in all directions (x, y, z) to search for entities
 * @return A list of entities of type [E] within the radius
 */
inline fun <reified E : Entity> Location.getNearbyEntitiesByType(radius: Double = 1.0) =
  world.getNearbyEntities(this, radius, radius, radius).filterIsInstance<E>()

/**
 * Gets all nearby entities of type [E] within the specified dimensions.
 *
 * @param E The type of entities to retrieve
 * @param x The radius in the x direction
 * @param y The radius in the y direction
 * @param z The radius in the z direction
 * @return A list of entities of type [E] within the specified dimensions
 */
inline fun <reified E : Entity> Location.getNearbyEntitiesByType(x: Double = 1.0, y: Double = 1.0, z: Double = 1.0) =
  world.getNearbyEntities(this, x, y, z).filterIsInstance<E>()

/**
 * Gets all nearby entities of type [E] within the specified radius that match the predicate.
 *
 * @param E The type of entities to retrieve
 * @param radius The radius in all directions (x, y, z) to search for entities
 * @param predicate A filter function to select specific entities
 * @return A list of entities of type [E] within the radius that match the predicate
 */
inline fun <reified E : Entity> Location.getNearbyEntitiesByType(
  radius: Double = 1.0,
  predicate: (E) -> Boolean = { true }
) =
  world.getNearbyEntities(this, radius, radius, radius).filterIsInstance<E>().filter(predicate)

/**
 * Gets all nearby entities of type [E] within the specified dimensions that match the predicate.
 *
 * @param E The type of entities to retrieve
 * @param x The radius in the x direction
 * @param y The radius in the y direction
 * @param z The radius in the z direction
 * @param predicate A filter function to select specific entities
 * @return A list of entities of type [E] within the specified dimensions that match the predicate
 */
inline fun <reified E : Entity> Location.getNearbyEntitiesByType(
  x: Double = 1.0,
  y: Double = 1.0,
  z: Double = 1.0,
  predicate: (E) -> Boolean = { true }
) =
  world.getNearbyEntities(this, x, y, z).filterIsInstance<E>().filter(predicate)
