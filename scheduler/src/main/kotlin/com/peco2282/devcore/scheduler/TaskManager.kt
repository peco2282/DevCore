package com.peco2282.devcore.scheduler

import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.plugin.Plugin
import java.util.*

/**
 * Manages scheduled tasks and their lifecycle.
 *
 * This class is responsible for tracking active tasks and automatically cancelling
 * them when certain events occur, such as a player quitting or a world unloading.
 *
 * @param plugin the [Plugin] instance associated with this manager
 */
class TaskManager(plugin: Plugin) {

  private val allTasks = mutableSetOf<TaskHandle>()
  private val playerTasks = mutableMapOf<UUID, MutableSet<TaskHandle>>()
  private val worldTasks = mutableMapOf<UUID, MutableSet<TaskHandle>>() // world UID

  init {
    plugin.on<PlayerQuitEvent> {
      playerTasks.remove(player.uniqueId)?.forEach { it.cancel() }
    }

    plugin.on<WorldUnloadEvent> {
      worldTasks.remove(world.uid)?.forEach { it.cancel() }
    }
  }

  /**
   * Tracks the specified task [handle].
   *
   * @param handle the [TaskHandle] to track
   */
  fun track(handle: TaskHandle) {
    allTasks += handle
  }

  /**
   * Tracks the specified task [handle] for the [player].
   *
   * The task will be automatically cancelled when the player quits the server.
   *
   * @param player the [Player] to associate the task with
   * @param handle the [TaskHandle] to track
   */
  fun trackPlayer(player: Player, handle: TaskHandle) {
    playerTasks.getOrPut(player.uniqueId) { mutableSetOf() }.add(handle)
    track(handle)
  }

  /**
   * Tracks the specified task [handle] for the [world].
   *
   * The task will be automatically cancelled when the world is unloaded.
   *
   * @param world the [World] to associate the task with
   * @param handle the [TaskHandle] to track
   */
  fun trackWorld(world: World, handle: TaskHandle) {
    worldTasks.getOrPut(world.uid) { mutableSetOf() }.add(handle)
    track(handle)
  }

  /**
   * Cancels all tracked tasks.
   *
   * This is typically used when the plugin is being disabled to ensure no tasks
   * continue to run.
   */
  fun cancelAll() = allTasks.forEach { it.cancel() }
}
