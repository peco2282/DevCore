package com.peco2282.devcore.util

import net.kyori.adventure.text.Component
import org.bukkit.entity.Entity


/**
 * Filters entities from this list that are within the specified radius from a center entity.
 *
 * @param filterInRadius The maximum distance (in blocks) from the center entity.
 * @param center The entity to measure distance from.
 * @return A list of entities within the specified radius.
 */
fun List<Entity>.rangeOf(filterInRadius: Double, center: Entity): List<Entity> =
  filter { it.location.distanceSquared(center.location) <= filterInRadius * filterInRadius }

/**
 * Gets all entities within the specified radius from this entity's location.
 *
 * @param filterInRadius The maximum distance (in blocks) from this entity.
 * @return A collection of entities within the specified radius.
 */
fun Entity.rangeOf(filterInRadius: Double): Collection<Entity> =
  world.getNearbyEntities(location, filterInRadius, filterInRadius, filterInRadius)

/**
 * Sends a string message to all entities in this collection.
 *
 * @param msg The message to send.
 */
fun Collection<Entity>.send(msg: String) = forEach { it.sendMessage(msg) }

/**
 * Sends a Component message to all entities in this collection.
 *
 * @param msg The Component message to send.
 */
fun Collection<Entity>.send(msg: Component) = forEach { it.sendMessage(msg) }
