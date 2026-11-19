package com.peco2282.devcore.display

import java.util.WeakHashMap
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.plugin.Plugin

internal class DisplayLifecycle private constructor(private val plugin: Plugin) : Listener {
  private val entities = linkedSetOf<Entity>()

  fun track(entity: Entity) { entities += entity }
  fun forget(entity: Entity) { entities -= entity }

  @EventHandler fun onWorldUnload(event: WorldUnloadEvent) = removeWorld(event.world)
  @EventHandler fun onPluginDisable(event: PluginDisableEvent) {
    if (event.plugin === plugin) removeAll()
  }

  private fun removeWorld(world: World) {
    entities.filter { it.world == world }.toList().forEach { it.remove(); entities -= it }
  }

  private fun removeAll() {
    entities.toList().forEach { it.remove() }
    entities.clear()
    synchronized(instances) { instances.remove(plugin) }
  }

  companion object {
    private val instances = WeakHashMap<Plugin, DisplayLifecycle>()
    fun of(plugin: Plugin): DisplayLifecycle = synchronized(instances) {
      instances.getOrPut(plugin) {
        DisplayLifecycle(plugin).also { plugin.server.pluginManager.registerEvents(it, plugin) }
      }
    }
  }
}
