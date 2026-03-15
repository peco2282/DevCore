package com.peco2282.devcore.scoreboard.api

import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scheduler.scheduler
import com.peco2282.devcore.scheduler.ticks
import com.peco2282.devcore.scoreboard.api.factory.ScoreboardFactory
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * A fallback implementation of [ScoreboardFactory] using the Paper API.
 */
internal object PaperScoreboardFactory: ScoreboardFactory {
  override fun createSidebar(
    title: () -> Component,
    lines: List<(Player) -> Component>,
    plugin: Plugin?,
    refreshInterval: Ticks?
  ): SidebarHandle {
    return PaperSidebarHandle(title, lines, plugin, refreshInterval)
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

class PaperSidebarHandle(
  private val title: () -> Component,
  private val lines: List<(Player) -> Component>,
  private val plugin: Plugin?,
  private val refreshInterval: Ticks?
) : SidebarHandle {

  private val viewers = ConcurrentHashMap.newKeySet<Player>()
  private var task: com.peco2282.devcore.scheduler.TaskHandle? = null
  private val objectiveName = "dc_" + UUID.randomUUID().toString().substring(0, 8)

  init {
    if (plugin != null && refreshInterval != null) {
      task = plugin.scheduler.timer(0.ticks, refreshInterval) {
        update()
      }
    }
  }

  override fun update() {
    viewers.forEach { updateFor(it) }
  }

  override fun show(player: Player) {
    if (viewers.add(player)) {
      updateFor(player)
    }
  }

  override fun hide(player: Player) {
    if (viewers.remove(player)) {
      player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
    }
  }

  override fun destroy() {
    task?.cancel()
    viewers.forEach { hide(it) }
    viewers.clear()
  }

  private fun updateFor(player: Player) {
    val scoreboard = if (player.scoreboard == Bukkit.getScoreboardManager().mainScoreboard) {
      Bukkit.getScoreboardManager().newScoreboard
    } else {
      player.scoreboard
    }

    val objective = scoreboard.getObjective(objectiveName) ?: scoreboard.registerNewObjective(
      objectiveName,
      Criteria.DUMMY,
      title()
    )
    
    objective.displayName(title())
    objective.displaySlot = DisplaySlot.SIDEBAR

    // Clear old scores (simplified approach)
    scoreboard.entries.forEach { scoreboard.resetScores(it) }

    for (i in lines.indices) {
      val component = lines[i](player)
      val scoreValue = lines.size - i
      // Use empty colors as entry names to allow duplicate components
      val entryName = i.toString(16).map { "§$it" }.joinToString("") + "§r"
      
      val team = scoreboard.getTeam("line_$i") ?: scoreboard.registerNewTeam("line_$i")
      team.addEntry(entryName)
      team.prefix(component)
      
      objective.getScore(entryName).score = scoreValue
    }

    if (player.scoreboard != scoreboard) {
      player.scoreboard = scoreboard
    }
  }
}
