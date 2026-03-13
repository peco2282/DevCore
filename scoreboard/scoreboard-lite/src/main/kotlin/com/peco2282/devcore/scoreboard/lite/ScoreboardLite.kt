package com.peco2282.devcore.scoreboard.lite

import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scheduler.scheduler
import com.peco2282.devcore.scheduler.ticks
import com.peco2282.devcore.scoreboard.api.BossBarHandle
import com.peco2282.devcore.scoreboard.api.ScoreboardApi
import com.peco2282.devcore.scoreboard.api.SidebarHandle
import com.peco2282.devcore.scoreboard.api.factory.ScoreboardFactory
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit.*
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object ScoreboardLite : ScoreboardFactory {

  fun init(plugin: Plugin) {
    ScoreboardApi.init(plugin, this)
  }

  override fun createSidebar(
    title: () -> Component,
    lines: List<(Player) -> Component>,
    plugin: Plugin?,
    refreshInterval: Ticks?
  ): SidebarHandle {
    return LiteSidebarHandle(title, lines, plugin, refreshInterval)
  }

  override fun createBossBar(
    title: (Player) -> Component,
    progress: (Player) -> Float,
    bar: BossBar,
    plugin: Plugin?,
    refreshInterval: Ticks?,
    visibilityCondition: ((Player) -> Boolean)?
  ): BossBarHandle {
    return com.peco2282.devcore.scoreboard.api.DefaultBossBarHandle(title, progress, bar, plugin, refreshInterval, visibilityCondition)
  }
}

private class LiteSidebarHandle(
  private val title: () -> Component,
  private val lines: List<(Player) -> Component>,
  private val plugin: Plugin?,
  private val refreshInterval: Ticks?
) : SidebarHandle {
  private val id = UUID.randomUUID().toString().substring(0, 16)
  private val viewers = ConcurrentHashMap<Player, Scoreboard>()

  override fun update() {
    viewers.forEach { (player, scoreboard) ->
      updateScoreboard(player, scoreboard)
    }
  }

  private fun updateScoreboard(player: Player, scoreboard: Scoreboard) {
    val objective = scoreboard.getObjective(id) ?: return
    objective.displayName(title())

    scoreboard.entries.forEach { scoreboard.resetScores(it) }

    val limitedLines = if (lines.size > 15) lines.take(15) else lines

    limitedLines.asReversed().forEachIndexed { index, lineFunc ->
      val lineContent = lineFunc(player)
      val entry = if (lineContent == Component.empty()) {
        " ".repeat(index + 1)
      } else {
        PlainTextComponentSerializer.plainText().serialize(lineContent)
      }
      val score = objective.getScore(entry)
      score.score = index
    }
  }

  override fun show(player: Player) {
    if (viewers.containsKey(player)) return

    if (plugin != null && refreshInterval != null && viewers.isEmpty()) {
      plugin.scheduler.timer(0.ticks, refreshInterval) {
        update()
      }
    }

    val scoreboard = getScoreboardManager().newScoreboard
    val objective = scoreboard.registerNewObjective(id, "dummy", title())
    objective.displaySlot = DisplaySlot.SIDEBAR

    viewers[player] = scoreboard
    updateScoreboard(player, scoreboard)
    player.scoreboard = scoreboard
  }

  override fun hide(player: Player) {
    if (viewers.remove(player) != null) {
      player.scoreboard = getScoreboardManager().mainScoreboard
    }
  }

  override fun destroy() {
    viewers.keys.forEach { it.scoreboard = getScoreboardManager().mainScoreboard }
    viewers.clear()
    ScoreboardApi.unregister(this)
  }
}
