package com.peco2282.devcore.scoreboard.api

import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scoreboard.api.builder.BossBarBuilder
import com.peco2282.devcore.scoreboard.api.builder.SidebarBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.Team

/**
 * DSL for managing scoreboard teams for player name decoration (Prefix/Suffix).
 *
 * @param name The name of the team.
 * @param block The builder block.
 */
fun Player.team(name: String, block: Team.() -> Unit) {
  val sb = if (scoreboard == Bukkit.getScoreboardManager().mainScoreboard) {
    Bukkit.getScoreboardManager().newScoreboard
  } else {
    scoreboard
  }

  val team = sb.getTeam(name) ?: sb.registerNewTeam(name)
  if (!team.hasEntry(this.name)) {
    team.addEntry(this.name)
  }
  team.block()

  if (scoreboard != sb) {
    scoreboard = sb
  }
}

/**
 * Creates and returns a [SidebarHandle] using a DSL.
 *
 * Example usage:
 * ```kotlin
 * val handle = sidebar(Component.text("My Sidebar")) {
 *     line(0, Component.text("Welcome!"))
 *     line(1) { player -> Component.text("Player: ${player.name}") }
 * }
 * handle.show(player)
 * ```
 *
 * @param title The title of the sidebar.
 * @param block The builder block to configure the sidebar.
 * @return A new [SidebarHandle].
 */
inline fun sidebar(title: Component, block: SidebarBuilder.() -> Unit): SidebarHandle =
  sidebar({ title }, block)

/**
 * Creates and returns a [SidebarHandle] using a DSL with a dynamic title.
 *
 * @param title A function providing the title of the sidebar.
 * @param block The builder block to configure the sidebar.
 * @return A new [SidebarHandle].
 */
inline fun sidebar(noinline title: () -> Component, block: SidebarBuilder.() -> Unit): SidebarHandle {
  return SidebarBuilder(title).apply(block).build()
}

/**
 * Creates and returns a [SidebarHandle] with auto-refresh enabled for the plugin.
 *
 * @receiver The plugin instance.
 * @param title The title of the sidebar.
 * @param block The builder block to configure the sidebar.
 * @return A new [SidebarHandle].
 */
inline fun Plugin.sidebar(title: Component, block: SidebarBuilder.() -> Unit): SidebarHandle =
  sidebar({ title }, block)

/**
 * Creates and returns a [SidebarHandle] with auto-refresh enabled for the plugin.
 *
 * @receiver The plugin instance.
 * @param title A function providing the title of the sidebar.
 * @param block The builder block to configure the sidebar.
 * @return A new [SidebarHandle].
 */
inline fun Plugin.sidebar(noinline title: () -> Component, block: SidebarBuilder.() -> Unit): SidebarHandle {
  return SidebarBuilder(title).apply { autoRefresh(this@sidebar) }.apply(block).build()
}

/**
 * Creates and returns a [SidebarHandle] with a specific refresh interval.
 *
 * @receiver The plugin instance.
 * @param refreshInterval The interval at which the sidebar should refresh.
 * @param title The title of the sidebar.
 * @param block The builder block to configure the sidebar.
 * @return A new [SidebarHandle].
 */
inline fun Plugin.sidebar(refreshInterval: Ticks, title: Component, block: SidebarBuilder.() -> Unit): SidebarHandle =
  sidebar(refreshInterval, { title }, block)

/**
 * Creates and returns a [SidebarHandle] with a specific refresh interval and dynamic title.
 *
 * @receiver The plugin instance.
 * @param refreshInterval The interval at which the sidebar should refresh.
 * @param title A function providing the title of the sidebar.
 * @param block The builder block to configure the sidebar.
 * @return A new [SidebarHandle].
 */
inline fun Plugin.sidebar(
  refreshInterval: Ticks,
  noinline title: () -> Component,
  block: SidebarBuilder.() -> Unit
): SidebarHandle {
  return SidebarBuilder(title).apply { autoRefresh(this@sidebar, refreshInterval) }.apply(block).build()
}

/**
 * Creates and returns a [BossBarHandle] using a DSL.
 *
 * Example usage:
 * ```kotlin
 * val handle = bossBar(Component.text("Boss Health")) {
 *     progress { player -> getBossHealth(player) }
 *     color(BossBar.Color.RED)
 *     overlay(BossBar.Overlay.PROGRESS)
 * }
 * handle.show(player)
 * ```
 *
 * @param block The builder block to configure the boss bar.
 * @return A new [BossBarHandle].
 */
inline fun bossBar(block: BossBarBuilder.() -> Unit): BossBarHandle =
  BossBarBuilder().apply(block).build()

/**
 * Creates and returns a [BossBarHandle] with a title.
 *
 * @param title The title of the boss bar.
 * @param block The builder block to configure the boss bar.
 * @return A new [BossBarHandle].
 */
inline fun bossBar(title: Component, block: BossBarBuilder.() -> Unit): BossBarHandle =
  bossBar({ title }, block)

/**
 * Creates and returns a [BossBarHandle] with a dynamic title.
 *
 * @param title A function providing the title of the boss bar.
 * @param block The builder block to configure the boss bar.
 * @return A new [BossBarHandle].
 */
inline fun bossBar(noinline title: () -> Component, block: BossBarBuilder.() -> Unit): BossBarHandle =
  BossBarBuilder().apply { title(title) }.apply(block).build()

/**
 * Creates and returns a [BossBarHandle] with auto-refresh enabled.
 *
 * @param block The builder block to configure the boss bar.
 * @return A new [BossBarHandle].
 */
inline fun Plugin.bossBar(block: BossBarBuilder.() -> Unit): BossBarHandle =
  BossBarBuilder().apply { autoRefresh(this@bossBar) }.apply(block).build()

/**
 * Creates and returns a [BossBarHandle] with a title and auto-refresh.
 *
 * @param title The title of the boss bar.
 * @param block The builder block to configure the boss bar.
 * @return A new [BossBarHandle].
 */
inline fun Plugin.bossBar(title: Component, block: BossBarBuilder.() -> Unit): BossBarHandle =
  bossBar({ title }, block)

/**
 * Creates and returns a [BossBarHandle] with a dynamic title and auto-refresh.
 *
 * @param title A function providing the title of the boss bar.
 * @param block The builder block to configure the boss bar.
 * @return A new [BossBarHandle].
 */
inline fun Plugin.bossBar(noinline title: () -> Component, block: BossBarBuilder.() -> Unit): BossBarHandle =
  BossBarBuilder().apply {
    title(title)
    autoRefresh(this@bossBar)
  }.apply(block).build()

/**
 * Creates and returns a [BossBarHandle] with a specific refresh interval.
 *
 * @receiver The plugin instance.
 * @param refreshInterval The interval at which the boss bar should refresh.
 * @param block The builder block to configure the boss bar.
 * @return A new [BossBarHandle].
 */
inline fun Plugin.bossBar(refreshInterval: Ticks, block: BossBarBuilder.() -> Unit): BossBarHandle =
  BossBarBuilder().apply { autoRefresh(this@bossBar, refreshInterval) }.apply(block).build()

/**
 * Creates and returns a [BossBarHandle] with a specific refresh interval and title.
 *
 * @receiver The plugin instance.
 * @param refreshInterval The interval at which the boss bar should refresh.
 * @param title The title of the boss bar.
 * @param block The builder block to configure the boss bar.
 * @return A new [BossBarHandle].
 */
inline fun Plugin.bossBar(refreshInterval: Ticks, title: Component, block: BossBarBuilder.() -> Unit): BossBarHandle =
  bossBar(refreshInterval, { title }, block)

/**
 * Creates and returns a [BossBarHandle] with a specific refresh interval and dynamic title.
 *
 * @receiver The plugin instance.
 * @param refreshInterval The interval at which the boss bar should refresh.
 * @param title A function providing the title of the boss bar.
 * @param block The builder block to configure the boss bar.
 * @return A new [BossBarHandle].
 */
inline fun Plugin.bossBar(
  refreshInterval: Ticks,
  noinline title: () -> Component,
  block: BossBarBuilder.() -> Unit
): BossBarHandle =
  BossBarBuilder().apply {
    title(title)
    autoRefresh(this@bossBar, refreshInterval)
  }.apply(block).build()
