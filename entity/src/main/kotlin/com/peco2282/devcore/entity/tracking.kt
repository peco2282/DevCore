package com.peco2282.devcore.entity

import com.peco2282.devcore.scheduler.TaskHandle
import com.peco2282.devcore.scheduler.scheduler
import com.peco2282.devcore.scheduler.ticks
import io.papermc.paper.entity.LookAnchor
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.plugin.Plugin

/**
 * Makes this entity follow another entity.
 *
 * @param plugin the plugin to register the task
 * @param target the entity to follow
 * @param speed the movement speed (used if this entity is a [Mob])
 * @param stopDistance the distance to stop following
 * @return a [TaskHandle] to control the tracking task
 */
fun Entity.follow(
  plugin: Plugin,
  target: Entity,
  speed: Double = 1.0,
  stopDistance: Double = 2.0
): TaskHandle {
  var handle: TaskHandle? = null
  val task: () -> Unit = {
    if (!this.isValid || !target.isValid || this.world != target.world) {
      handle?.cancel()
    } else {
      val distance = this.location.distance(target.location)
      if (distance > stopDistance) {
        if (this is Mob) {
          this.pathfinder.moveTo(target.location, speed)
        } else {
          val direction = target.location.toVector().subtract(this.location.toVector()).normalize()
          this.velocity = direction.multiply(0.2 * speed)
        }
      } else {
        if (this is Mob) {
          this.pathfinder.stopPathfinding()
        }
      }
    }
  }
  handle = plugin.scheduler.timer(0.ticks, 5.ticks, task)
  return handle
}

///**
// * Makes this entity always look at another entity.
// *
// * @param plugin the plugin to register the task
// * @param target the entity to look at
// * @return a [TaskHandle] to control the look-at task
// */
//fun Entity.lookAt(
//  plugin: Plugin,
//  target: Entity
//): TaskHandle {
//  var handle: TaskHandle? = null
//  val task: () -> Unit = {
//    if (!this.isValid || !target.isValid || this.world != target.world) {
//      handle?.cancel()
//    } else {
//      if (this is LivingEntity) {
//        val loc = target.location
//        this.lookAt(loc.x, loc.y, loc.z, LookAnchor.EYES)
//      } else {
//        val location = this.location
//        val direction = target.location.toVector().subtract(location.toVector())
//        location.setDirection(direction)
//        this.teleport(location)
//      }
//    }
//  }
//  handle = plugin.scheduler.timer(0.ticks, 1.ticks, task)
//  return handle
//}
