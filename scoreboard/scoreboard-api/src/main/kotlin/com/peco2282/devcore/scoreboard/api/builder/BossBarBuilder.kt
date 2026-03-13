package com.peco2282.devcore.scoreboard.api.builder

import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scheduler.ticks
import com.peco2282.devcore.scoreboard.api.BossBarHandle
import com.peco2282.devcore.scoreboard.api.ScoreboardApi
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.concurrent.CopyOnWriteArrayList

@ScoreboardDsl
class BossBarBuilder {
  private var title: (Player) -> Component = { Component.empty() }
  private var color: BossBar.Color? = null
  private var overlay: BossBar.Overlay? = null
  private var progress: (Player) -> Float = { 0.0f }
  private var flags: MutableList<BossBar.Flag> = mutableListOf()
  private val listeners = CopyOnWriteArrayList<BossBar.Listener>()
  private var players: MutableSet<Player> = mutableSetOf()
  private var refreshInterval: Ticks? = null
  private var plugin: Plugin? = null
  private var visibilityCondition: ((Player) -> Boolean)? = null

  infix fun title(title: Component) = apply {
    this.title = { title }
  }

  infix fun title(title: () -> Component) = apply {
    this.title = { title() }
  }

  infix fun title(title: (Player) -> Component) = apply {
    this.title = title
  }

  infix fun color(color: BossBar.Color) = apply {
    this.color = color
  }

  fun pink() = color(BossBar.Color.PINK)
  fun blue() = color(BossBar.Color.BLUE)
  fun green() = color(BossBar.Color.GREEN)
  fun yellow() = color(BossBar.Color.YELLOW)
  fun red() = color(BossBar.Color.RED)
  fun purple() = color(BossBar.Color.PURPLE)
  fun white() = color(BossBar.Color.WHITE)

  infix fun overlay(overlay: BossBar.Overlay) = apply {
    this.overlay = overlay
  }

  infix fun progress(progress: Float) = apply {
    this.progress = { progress }
  }

  infix fun progress(progress: () -> Float) = apply {
    this.progress = { progress() }
  }

  infix fun progress(progress: (Player) -> Float) = apply {
    this.progress = progress
  }

  fun autoRefresh(plugin: Plugin, interval: Ticks = 20.ticks) = apply {
    this.plugin = plugin
    this.refreshInterval = interval
  }

  fun filter(condition: (Player) -> Boolean) = apply {
    this.visibilityCondition = condition
  }

  infix fun flag(flag: BossBar.Flag) = apply {
    flags.add(flag)
  }

  infix fun flags(flags: List<BossBar.Flag>) = apply {
    this.flags.addAll(flags)
  }

  infix fun players(players: Set<Player>) = apply {
    this.players = players.toMutableSet()
  }

  infix fun player(player: Player) = apply {
    players.add(player)
  }

  infix fun listener(listener: BossBar.Listener) = apply {
    listeners.add(listener)
  }

  infix fun listeners(listeners: List<BossBar.Listener>) = apply {
    this.listeners.addAll(listeners)
  }

  fun build(): BossBarHandle {
    val bar = BossBar.bossBar(
      Component.empty(),
      0.0f,
      color ?: BossBar.Color.PINK,
      overlay ?: BossBar.Overlay.PROGRESS,
      flags.toSet(),
    )
    listeners.forEach { bar.addListener(it) }

    val handle = ScoreboardApi.factory().createBossBar(
      title,
      progress,
      bar,
      plugin,
      refreshInterval,
      visibilityCondition
    )

    ScoreboardApi.register(handle)
    players.forEach { p -> handle.show(p) }
    return handle
  }
}
