package com.peco2282.testplugin

import com.peco2282.devcore.adventure.component
import com.peco2282.devcore.adventure.withStyle
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.util.UUID

object EventListener {
  val plugin by lazy { TestPlugin.instance }

  init {
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
      Component.text("")
      
      player.sendMessage(msg)
    }
  }
}