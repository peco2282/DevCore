package com.peco2282.testplugin

import com.peco2282.devcore.display.Hologram
import com.peco2282.devcore.display.hologram
import com.peco2282.devcore.game.Minigame
import com.peco2282.devcore.game.minigame
import com.peco2282.devcore.region.Region
import com.peco2282.devcore.region.cuboid
import com.peco2282.devcore.region.regions
import com.peco2282.devcore.teleport.TeleportWarmupHandle
import com.peco2282.devcore.teleport.teleportWithWarmup
import kotlin.time.Duration.Companion.seconds
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/** Compile-checked examples for the display, region, teleport, and game modules. */
object ChapterModules {
  lateinit var spawnRegion: Region
    private set
  lateinit var game: Minigame
    private set

  fun install(plugin: Plugin) {
    val world = plugin.server.worlds.first()
    val spawn = world.spawnLocation
    spawnRegion = cuboid(
      world,
      spawn.clone().add(-16.0, -8.0, -16.0),
      spawn.clone().add(16.0, 16.0, 16.0),
    )
    plugin.regions().add("test-spawn", spawnRegion)

    game = plugin.minigame("test-round") {
      minPlayers = 2
      maxPlayers = 12
      lobby { countdown = 5.seconds }
      running {
        duration = 60.seconds
        endWhen { alive.size <= 1 }
      }
      ending { duration = 5.seconds }
      teams("red", "blue")
    }
  }

  fun showHologram(plugin: Plugin, player: Player): Hologram =
    plugin.hologram(player.location.clone().add(0.0, 2.0, 0.0)) {
      line(Component.text("DevCore", NamedTextColor.GOLD))
      line { viewer -> Component.text("Viewer: ${viewer.name}") }
      billboard = Display.Billboard.CENTER
      visibleTo(player)
    }

  fun teleportHome(plugin: Plugin, player: Player, target: Location): TeleportWarmupHandle =
    player.teleportWithWarmup(plugin, target, 3.seconds) {
      cancelOnMove(0.5)
      cancelOnDamage()
      cooldown = 30.seconds
    }
}
