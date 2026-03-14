package com.peco2282.testplugin

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import com.peco2282.devcore.adventure.component
import com.peco2282.devcore.adventure.withStyle
import com.peco2282.devcore.scheduler.scheduler
import com.peco2282.devcore.scheduler.ticks
import com.peco2282.devcore.scoreboard.api.bossBar
import com.peco2282.devcore.scoreboard.api.sidebar
import com.peco2282.devcore.scoreboard.nms.NMSProvider
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent

object EventListener {
  val plugin by lazy { TestPlugin.instance }

  init {
    NMSProvider.init(plugin)

    plugin.on<PlayerJoinEvent> {
      // devcore-adventure
      val msg = component {
        text("Hello,") withStyle { yellow() }; space();
        +player.name withStyle {
          green()
          showItem(
            Key.key("minecraft", "pig_spawn_egg"),
            1
          )
          val loc = player.world.spawnLocation
          runCommand("/tp ${player.name} ${loc.x} ${loc.y} ${loc.z}")
        }
      }
      player.sendMessage(msg)

      plugin.sidebar(5.ticks, component { text("Test Sidebar") }) {
        line { p: Player -> component { text("Player: ${p.name}") withStyle { green() } } }
        line { _: Player -> component { text("Online: ${plugin.server.onlinePlayers.size}") } }
        line { _: Player -> component { text("World: ${player.world.name}") } }
        line { _: Player -> component { text("Location: ${player.location}") } }
      }.show(player)

      var i = 0
      plugin.bossBar(20.ticks) {
        title { p: Player ->
          component {
            text("Hello, ${p.name}")
            space()
            text("Progress: $i") withStyle { yellow() }
          }
        }
        pink()
        progress { _: Player ->
          (i % 10) / 10.0f
        }
        filter { p -> p.world.name == "world" }
      }.show(player)

      plugin.scheduler.timer(0.ticks, 20.ticks) {
        i++
      }
    }
    plugin.on<PlayerJumpEvent> {

    }
  }
}