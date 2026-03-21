package com.peco2282.devcore.scoreboard.nms.v1_20_3

import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scoreboard.api.BossBarHandle
import com.peco2282.devcore.scoreboard.api.DefaultBossBarHandle
import com.peco2282.devcore.scoreboard.api.SidebarHandle
import com.peco2282.devcore.scoreboard.api.factory.ScoreboardNMSFactory
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class NMSScoreboardFactoryImpl : ScoreboardNMSFactory {
  override fun createSidebar(
    title: () -> Component,
    lines: List<(Player) -> Component>,
    plugin: Plugin?,
    refreshInterval: Ticks?
  ): SidebarHandle {
    return NMSPacketHandlerImpl(title, lines, plugin, refreshInterval)
  }

  override fun createBossBar(
    title: (Player) -> Component,
    progress: (Player) -> Float,
    bar: BossBar,
    plugin: Plugin?,
    refreshInterval: Ticks?,
    visibilityCondition: ((Player) -> Boolean)?
  ): BossBarHandle {
    return DefaultBossBarHandle(title, progress, bar, plugin, refreshInterval, visibilityCondition)
  }
}
