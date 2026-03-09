package com.peco2282.testplugin

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

inline fun <reified T : Event> Plugin.on(
  priority: EventPriority = EventPriority.NORMAL,
  noinline handler: T.() -> Unit
) {
  Bukkit.getPluginManager().registerEvent(
    T::class.java,
    object : Listener {},
    priority,
    { _, event ->
      if (event is T) {
        event.handler()
      }
    },
    this,
    false
  )
}