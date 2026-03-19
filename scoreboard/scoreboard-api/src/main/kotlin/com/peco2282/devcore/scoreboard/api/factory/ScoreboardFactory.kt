package com.peco2282.devcore.scoreboard.api.factory

import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scoreboard.api.BossBarHandle
import com.peco2282.devcore.scoreboard.api.SidebarHandle
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * A factory interface for creating sidebar and boss bar handles.
 * Implementations should handle the underlying display logic (e.g., via NMS or Bukkit API).
 */
interface ScoreboardFactory {
  /**
   * Creates a [SidebarHandle].
   *
   * @param title A function providing the sidebar title.
   * @param lines A list of functions providing the content for each line.
   * @param plugin The plugin instance for scheduling updates (optional).
   * @param refreshInterval The interval at which the sidebar should refresh (optional).
   * @return A new [SidebarHandle].
   */
  fun createSidebar(
    title: () -> Component,
    lines: List<(Player) -> Component>,
    plugin: Plugin?,
    refreshInterval: Ticks?
  ): SidebarHandle

  /**
   * Creates a [BossBarHandle].
   *
   * @param title A function providing the boss bar title for a player.
   * @param progress A function providing the progress value for a player.
   * @param bar The [BossBar] instance.
   * @param plugin The plugin instance for scheduling updates (optional).
   * @param refreshInterval The interval at which the boss bar should refresh (optional).
   * @param visibilityCondition A predicate determining if the bar should be visible to a player (optional).
   * @return A new [BossBarHandle].
   */
  fun createBossBar(
    title: (Player) -> Component,
    progress: (Player) -> Float,
    bar: BossBar,
    plugin: Plugin?,
    refreshInterval: Ticks?,
    visibilityCondition: ((Player) -> Boolean)?
  ): BossBarHandle
}
