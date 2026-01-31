package com.peco2282.devcore.sheduler

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

internal inline infix fun <reified E: Event> Plugin.on(crossinline action: E.() -> Unit) {
  Bukkit.getPluginManager().registerEvent(
    E::class.java,
    object : Listener {},
    EventPriority.NORMAL,
    { _, event ->
      if (event is E) action(event)
    },
    this,
    false
  )
}

val Plugin.scheduler: Scheduler
  get() = PluginRegistory.get(this)

val Plugin.taskManager: TaskManager
  get() = scheduler.manager

val Plugin.taskCreate get() = TaskBuilder(this)
fun Player.taskAfter(plugin: Plugin, delay: Ticks, task: () -> Unit): TaskHandle {
  val handle = plugin.scheduler.later(delay, task)
  plugin.taskManager.trackPlayer(this, handle)
  return handle
}

fun World.taskTimer(plugin: Plugin, delay: Ticks, period: Ticks, task: () -> Unit): TaskHandle {
  val handle = plugin.scheduler.timer(delay, period, task)
  plugin.taskManager.trackWorld(this, handle)
  return handle
}

fun Plugin.removeScheduler() = PluginRegistory.remove(this)
