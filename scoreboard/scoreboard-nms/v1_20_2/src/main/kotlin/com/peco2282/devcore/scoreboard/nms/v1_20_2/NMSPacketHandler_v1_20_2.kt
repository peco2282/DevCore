package com.peco2282.devcore.scoreboard.nms.v1_20_2

import com.peco2282.devcore.scheduler.TaskHandle
import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scheduler.scheduler
import com.peco2282.devcore.scoreboard.api.SidebarHandle
import io.papermc.paper.adventure.AdventureComponent
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.server.ServerScoreboard
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Suppress("ClassName")
class NMSPacketHandler_v1_20_2(
  private val title: () -> Component,
  private val lines: List<(Player) -> Component>,
  private val plugin: Plugin?,
  private val refreshInterval: Ticks?
) : SidebarHandle {

  private val objectiveName = "dc_" + UUID.randomUUID().toString().substring(0, 8)
  private val viewers = ConcurrentHashMap.newKeySet<Player>()
  private var task: TaskHandle? = null

  private val scoreboard = Scoreboard()
  private val dummyObjective = Objective(
    scoreboard,
    objectiveName,
    ObjectiveCriteria.DUMMY,
    AdventureComponent(title()),
    ObjectiveCriteria.RenderType.INTEGER
  )

  init {
    if (plugin != null && refreshInterval != null) {
      task = plugin.scheduler.timerAsync(Ticks(0), refreshInterval) {
        update()
      }
    }
  }

  override fun update() {
    viewers.forEach { updateFor(it) }
  }

  override fun show(player: Player) {
    if (viewers.add(player)) {
      sendInitPackets(player)
      updateFor(player)
    }
  }

  override fun hide(player: Player) {
    if (viewers.remove(player)) {
      sendRemovePackets(player)
    }
  }

  override fun destroy() {
    task?.cancel()
    viewers.forEach { sendRemovePackets(it) }
    viewers.clear()
  }

  private fun updateFor(player: Player) {
    val craftPlayer = player as CraftPlayer
    val connection = craftPlayer.handle.connection

    // Objectiveの更新（タイトル）
    val updatePacket = ClientboundSetObjectivePacket(
      Objective(
        scoreboard,
        objectiveName,
        ObjectiveCriteria.DUMMY,
        AdventureComponent(title()),
        ObjectiveCriteria.RenderType.INTEGER
      ),
      ClientboundSetObjectivePacket.METHOD_CHANGE
    )
    connection.send(updatePacket)

    // 行の更新
    for (i in lines.indices) {
      val component = lines[i](player)
      val scoreValue = lines.size - i

      val entryName = i.toString(16).map { "§$it" }.joinToString("") + "§r"

      val team = PlayerTeam(scoreboard, "dc_t_$i")
      team.playerPrefix = AdventureComponent(component)
      team.players.add(entryName)

      val teamPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true)
      connection.send(teamPacket)

      val packet = ClientboundSetScorePacket(
        ServerScoreboard.Method.CHANGE,
        objectiveName,
        entryName,
        scoreValue,
      )
      connection.send(packet)
    }
    val displayPacket = ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, dummyObjective)
    connection.send(displayPacket)
  }

  private fun sendInitPackets(player: Player) {
    val craftPlayer = player as CraftPlayer
    val connection = craftPlayer.handle.connection

    // 1. Objective作成
    val addPacket = ClientboundSetObjectivePacket(dummyObjective, ClientboundSetObjectivePacket.METHOD_ADD)
    connection.send(addPacket)

    // 2. 表示スロット設定
    val displayPacket = ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, dummyObjective)
    connection.send(displayPacket)
  }

  private fun sendRemovePackets(player: Player) {
    val craftPlayer = player as CraftPlayer
    val connection = craftPlayer.handle.connection

    val removePacket = ClientboundSetObjectivePacket(dummyObjective, ClientboundSetObjectivePacket.METHOD_REMOVE)
    connection.send(removePacket)

    for (i in lines.indices) {
      val team = PlayerTeam(scoreboard, "dc_t_$i")
      val removeTeamPacket = ClientboundSetPlayerTeamPacket.createRemovePacket(team)
      connection.send(removeTeamPacket)
    }
  }
}
