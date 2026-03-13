package com.peco2282.devcore.scoreboard.api.factory

import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scoreboard.api.BossBarHandle
import com.peco2282.devcore.scoreboard.api.SidebarHandle
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

interface ScoreboardFactory {
  fun createSidebar(
    title: () -> Component,
    lines: List<(Player) -> Component>,
    plugin: Plugin?,
    refreshInterval: Ticks?
  ): SidebarHandle

  fun createBossBar(
    title: (Player) -> Component,
    progress: (Player) -> Float,
    bar: BossBar,
    plugin: Plugin?,
    refreshInterval: Ticks?,
    visibilityCondition: ((Player) -> Boolean)?
  ): BossBarHandle
}
