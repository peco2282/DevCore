package com.peco2282.testplugin

import com.peco2282.devcore.item.customItems
import com.peco2282.devcore.pdc.pdc
import com.peco2282.devcore.storage.playerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/** Persistent per-player data, saved as playerdata/<uuid>.yml. */
data class PlayerStats(var joins: Int = 0, var rubyHits: Int = 0)

/** Stored in the player's own persistent data container (survives without any file of ours). */
var Player.visits: Int by pdc(NamespacedKey.fromString("testplugin:visits")!!, 0)

/** Examples for the `pdc`, `item` and `storage` modules. Call [install] from onEnable. */
object NewModules {
  lateinit var stats: com.peco2282.devcore.storage.PlayerData<PlayerStats>
  lateinit var items: com.peco2282.devcore.item.CustomItems

  fun install(plugin: Plugin) {
    stats = plugin.playerData<PlayerStats>()

    items = plugin.customItems()
    items.register("ruby_sword", Material.DIAMOND_SWORD) {
      appearance {
        displayName = Component.text("Ruby Sword", NamedTextColor.RED)
        enchant(Enchantment.SHARPNESS, 3)
      }
      onAttack { event ->
        event.damage *= 1.5
        (event.damager as? Player)?.let { stats[it].rubyHits++ }
      }
      shaped(" R", " R", " S") {
        ingredient('R', Material.REDSTONE_BLOCK)
        ingredient('S', Material.STICK)
      }
    }
  }

  fun describe(player: Player): Component {
    val s = stats[player]
    return Component.text("joins=${s.joins} rubyHits=${s.rubyHits} visits=${player.visits}")
  }

  fun onJoin(player: Player) {
    stats[player].joins++
    player.visits++
  }
}

