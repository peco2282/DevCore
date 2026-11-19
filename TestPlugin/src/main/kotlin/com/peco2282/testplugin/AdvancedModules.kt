package com.peco2282.testplugin

import com.peco2282.devcore.config.Configs
import com.peco2282.devcore.config.migration.ConfigVersion
import com.peco2282.devcore.loot.LootTable
import com.peco2282.devcore.loot.lootTable
import com.peco2282.devcore.merchant.openShop
import com.peco2282.devcore.nametag.nametag
import com.peco2282.devcore.nametag.tablist
import com.peco2282.devcore.util.animation.Easing
import com.peco2282.devcore.util.animation.tween
import java.io.File
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

/** Compile-checked, practical examples for chapters 7–11. */
object AdvancedModules {
  data class RewardContext(val vip: Boolean, val luckBonus: Double = 0.0)

  val rewards: LootTable<RewardContext> = lootTable {
    rolls = 2..4
    weight(70) { item(Material.IRON_INGOT, 1..3) }
    weight(25) { item(Material.GOLD_INGOT, 1..2) }
    weight(5, condition = { it.vip }) { item(Material.DIAMOND) }
    guaranteed { item(Material.BREAD, 1..2) }
    bonus(0.10) { item(Material.EMERALD) }
  }

  data class MigratedExample(
    @ConfigVersion val version: Int = 3,
    val welcome: String = "Welcome",
    val cooldownTicks: Int = 60
  )

  fun install(plugin: Plugin) {
    plugin.tablist {
      refreshTicks = 20
      header(Component.text("DevCore Test", NamedTextColor.GOLD))
      footer { viewer -> Component.text("Ping: ${viewer.ping}ms", NamedTextColor.GRAY) }
    }
  }

  fun onJoin(plugin: Plugin, player: Player) {
    plugin.nametag(player) {
      prefix = Component.text("[TEST] ", NamedTextColor.AQUA)
      color = NamedTextColor.YELLOW
      sortKey = if (player.isOp) "01-admin" else "50-user"
    }
  }

  fun giveRewards(player: Player, vip: Boolean, random: Random = Random.Default) =
    rewards.rollTo(player.inventory, random, RewardContext(vip)).also { delivery ->
      delivery.leftovers.forEach { player.world.dropItemNaturally(player.location, it) }
    }

  fun openRewardShop(plugin: Plugin, player: Player) {
    player.openShop(plugin, Component.text("Reward Exchange")) {
      trade(ItemStack(Material.EMERALD, 5), ItemStack(Material.DIAMOND)) {
        maxUses = 10
        experienceReward = false
        onTrade { event -> event.player.sendMessage(Component.text("Trade completed!")) }
      }
      trade(
        listOf(ItemStack(Material.IRON_INGOT, 8), ItemStack(Material.COAL, 4)),
        ItemStack(Material.DIAMOND_PICKAXE)
      )
    }
  }

  fun animateValue(plugin: Plugin, update: (Double) -> Unit) =
    tween(0.0, 1.0, 2.seconds, Easing.OUT_CUBIC).start(plugin, periodTicks = 1, update)

  fun loadMigrated(file: File): MigratedExample = Configs.load(file) {
    migrations {
      from(1) { rename("message", "welcome") }
      from(2) {
        rename("cooldown", "cooldownTicks")
        transform("cooldownTicks") { (it as Int) * 20 }
      }
    }
  }
}
