package com.peco2282.devcore.util

import net.kyori.adventure.text.Component
import org.bukkit.entity.Entity

fun List<Entity>.rangeOf(filterInRadius: Double, center: Entity): List<Entity> =
  filter { it.location.distanceSquared(center.location) <= filterInRadius * filterInRadius }

fun Entity.rangeOf(filterInRadius: Double): Collection<Entity> =
  world.getNearbyEntities(location, filterInRadius, filterInRadius, filterInRadius)

fun Collection<Entity>.send(msg: String) = forEach { it.sendMessage(msg) }
fun Collection<Entity>.send(msg: Component) = forEach { it.sendMessage(msg) }
