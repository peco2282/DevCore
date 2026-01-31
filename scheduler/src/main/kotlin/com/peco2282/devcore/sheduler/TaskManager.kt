package com.peco2282.devcore.sheduler

import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.plugin.Plugin
import java.util.UUID

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

  fun track(handle: TaskHandle) {
    allTasks += handle
  }

  fun trackPlayer(player: Player, handle: TaskHandle) {
    playerTasks.getOrPut(player.uniqueId) { mutableSetOf() }.add(handle)
    track(handle)
  }

  fun trackWorld(world: World, handle: TaskHandle) {
    worldTasks.getOrPut(world.uid) { mutableSetOf() }.add(handle)
    track(handle)
  }

  fun cancelAll() = allTasks.forEach { it.cancel() }
}
