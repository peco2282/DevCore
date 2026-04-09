package com.peco2282.devcore.entity

import com.peco2282.devcore.scheduler.TaskHandle
import com.peco2282.devcore.scheduler.scheduler
import com.peco2282.devcore.scheduler.ticks
import org.bukkit.Particle
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

/**
 * Adds a particle trail to this entity.
 *
 * @param plugin the plugin to register the task
 * @param particle the particle to spawn
 * @param count the number of particles per tick
 * @param offset the offset of the particle
 * @param extra the extra data (e.g. speed)
 * @param onlyOnMove if true, particles will only be spawned when the entity is moving
 * @return a [TaskHandle] to control the trail task
 */
fun Entity.addTrail(
  plugin: Plugin,
  particle: Particle,
  count: Int = 1,
  offset: Double = 0.0,
  extra: Double = 0.0,
  onlyOnMove: Boolean = true
): TaskHandle {
  var lastLoc = this.location
  var handle: TaskHandle? = null
  val task: () -> Unit = {
    if (!this.isValid) {
      handle?.cancel()
    } else {
      val currentLoc = this.location
      if (!onlyOnMove || lastLoc.distanceSquared(currentLoc) > 0.0001) {
        this.world.spawnParticle(particle, currentLoc, count, offset, offset, offset, extra)
      }
    }
  }
  handle = plugin.scheduler.timer(0.ticks, 1.ticks, task)
  return handle
}
