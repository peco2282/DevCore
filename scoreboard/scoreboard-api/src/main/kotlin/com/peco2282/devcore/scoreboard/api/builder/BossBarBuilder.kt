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

/**
 * A builder class for creating [BossBarHandle] instances using a DSL.
 */
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

  /**
   * Sets a static title for the boss bar.
   */
  infix fun title(title: Component) = apply {
    this.title = { title }
  }

  /**
   * Sets a dynamic title for the boss bar.
   */
  infix fun title(title: () -> Component) = apply {
    this.title = { title() }
  }

  /**
   * Sets a title that can change based on the player.
   */
  infix fun title(title: (Player) -> Component) = apply {
    this.title = title
  }

  /**
   * Sets the color of the boss bar.
   */
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

  /**
   * Sets the overlay of the boss bar.
   */
  infix fun overlay(overlay: BossBar.Overlay) = apply {
    this.overlay = overlay
  }

  /**
   * Sets a static progress for the boss bar (0.0 to 1.0).
   */
  infix fun progress(progress: Float) = apply {
    this.progress = { progress }
  }

  /**
   * Sets a dynamic progress for the boss bar.
   */
  infix fun progress(progress: () -> Float) = apply {
    this.progress = { progress() }
  }

  /**
   * Sets a progress that can change based on the player.
   */
  infix fun progress(progress: (Player) -> Float) = apply {
    this.progress = progress
  }

  /**
   * Enables automatic refreshing of the boss bar.
   *
   * @param plugin The plugin instance to run the task.
   * @param interval The refresh interval (default: 20 ticks).
   */
  fun autoRefresh(plugin: Plugin, interval: Ticks = 20.ticks) = apply {
    this.plugin = plugin
    this.refreshInterval = interval
  }

  /**
   * Sets a visibility condition for the boss bar.
   *
   * @param condition A predicate that returns true if the bar should be shown to the player.
   */
  fun filter(condition: (Player) -> Boolean) = apply {
    this.visibilityCondition = condition
  }

  /**
   * Adds a flag to the boss bar.
   */
  infix fun flag(flag: BossBar.Flag) = apply {
    flags.add(flag)
  }

  /**
   * Adds multiple flags to the boss bar.
   */
  infix fun flags(flags: List<BossBar.Flag>) = apply {
    this.flags.addAll(flags)
  }

  /**
   * Sets the initial set of players to show the boss bar to.
   */
  infix fun players(players: Set<Player>) = apply {
    this.players = players.toMutableSet()
  }

  /**
   * Adds a player to show the boss bar to.
   */
  infix fun player(player: Player) = apply {
    players.add(player)
  }

  /**
   * Adds a listener to the boss bar.
   */
  infix fun listener(listener: BossBar.Listener) = apply {
    listeners.add(listener)
  }

  /**
   * Adds multiple listeners to the boss bar.
   */
  infix fun listeners(listeners: List<BossBar.Listener>) = apply {
    this.listeners.addAll(listeners)
  }

  /**
   * Builds and registers the [BossBarHandle].
   */
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
