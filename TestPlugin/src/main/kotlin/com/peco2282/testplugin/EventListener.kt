package com.peco2282.testplugin

import com.peco2282.adventure.component
import com.peco2282.adventure.withStyle
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.event.player.PlayerJoinEvent

object EventListener {
  val plugin by lazy { TestPlugin.instance }

  init {
    plugin.on<PlayerJoinEvent> {
      val msg = component {
        text("Hello,") withStyle { yellow() }; space();
        text(player.name) withStyle {
          green()
          hoverEvent(HoverEvent.showText(Component.text("Click to teleport to spawn!")))
          val loc = player.world.spawnLocation
          clickEvent(ClickEvent.runCommand("/tp ${player.name} ${loc.x} ${loc.y} ${loc.z}"))
        }
        +player.name withStyle {
          bold(); red()
        }
      }
      player.sendMessage(msg)
    }
  }
}