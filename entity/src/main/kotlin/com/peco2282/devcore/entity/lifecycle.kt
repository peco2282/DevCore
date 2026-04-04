package com.peco2282.devcore.entity

import com.peco2282.devcore.event.on
import com.peco2282.devcore.scheduler.TaskHandle
import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scheduler.scheduler
import com.peco2282.devcore.scheduler.ticks
import org.bukkit.entity.Entity
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.Plugin
import kotlin.time.Duration

/**
 * Registers an event listener for this specific entity.
 * The listener will be automatically unregistered if the entity is no longer valid.
 *
 * @param plugin the plugin to register the listener
 * @param handle the event handler
 */
inline fun <reified T : Event> Entity.onEvent(
  plugin: Plugin,
  crossinline getEntity: T.() -> Entity?,
  noinline handle: T.() -> Unit
) {
  plugin.on<T> {
    filter { getEntity(this) == this@onEvent }
    takeWhile { this@onEvent.isValid }
    handle(handle)
  }
}

/**
 * Executes the [handle] when this entity dies.
 */
fun Entity.onDeath(plugin: Plugin, handle: EntityDeathEvent.() -> Unit) {
  onEvent<EntityDeathEvent>(plugin, { entity }, handle)
}

/**
 * Executes the [handle] when this entity takes damage.
 */
fun Entity.onDamage(plugin: Plugin, handle: EntityDamageEvent.() -> Unit) {
  onEvent<EntityDamageEvent>(plugin, { entity }, handle)
}

/**
 * Automatically removes this entity after the specified [duration].
 */
fun Entity.removeAfter(plugin: Plugin, duration: Duration): TaskHandle {
  return plugin.scheduler.later((duration.inWholeMilliseconds / 50).ticks) {
    if (isValid) remove()
  }
}

/**
 * Executes the [action] every [period] ticks as long as the entity is valid.
 */
fun <E: Entity> E.onTick(
  plugin: Plugin,
  period: Ticks = 1.ticks,
  delay: Ticks = 0.ticks,
  action: E.() -> Unit
): TaskHandle {
  var handle: TaskHandle? = null
  val task: () -> Unit = {
    if (!isValid) {
      handle?.cancel()
    } else {
      action()
    }
  }
  handle = plugin.scheduler.timer(delay, period, task)
  return handle
}
