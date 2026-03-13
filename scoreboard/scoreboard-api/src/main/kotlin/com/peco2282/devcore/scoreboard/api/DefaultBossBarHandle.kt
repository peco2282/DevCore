package com.peco2282.devcore.scoreboard.api

import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scheduler.scheduler
import com.peco2282.devcore.scheduler.ticks
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.concurrent.CopyOnWriteArrayList

class DefaultBossBarHandle(
  private val title: (Player) -> Component,
  private val progress: (Player) -> Float,
  private val bar: BossBar,
  private val plugin: Plugin?,
  private val refreshInterval: Ticks?,
  private val visibilityCondition: ((Player) -> Boolean)?
) : BossBarHandle {
  private val viewers = CopyOnWriteArrayList<Player>()

  override fun update() {
    if (visibilityCondition != null) {
      org.bukkit.Bukkit.getOnlinePlayers().forEach { player ->
        if (visibilityCondition.invoke(player)) {
          show(player)
        } else {
          hide(player)
        }
      }
    }

    viewers.forEach { player ->
      bar.name(title(player))
      bar.progress(progress(player))
    }
  }

  override fun show(player: Player) {
    if (!viewers.contains(player)) {
      if (plugin != null && refreshInterval != null && viewers.isEmpty()) {
        plugin.scheduler.timer(0.ticks, refreshInterval) {
          update()
        }
      }
      viewers.add(player)
      player.showBossBar(bar)
      bar.name(title(player))
      bar.progress(progress(player))
    }
  }

  override fun hide(player: Player) {
    if (viewers.remove(player)) {
      player.hideBossBar(bar)
    }
  }

  override fun destroy() {
    viewers.forEach { it.hideBossBar(bar) }
    viewers.clear()
    ScoreboardApi.unregister(this)
  }
}
