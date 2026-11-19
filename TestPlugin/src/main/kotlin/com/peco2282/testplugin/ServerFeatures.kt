package com.peco2282.testplugin

import com.peco2282.devcore.cache.ttlCache
import com.peco2282.devcore.cooldown.persistent.PersistentCooldowns
import com.peco2282.devcore.debug.debugController
import com.peco2282.devcore.debug.debugLogger
import com.peco2282.devcore.kit.kits
import com.peco2282.devcore.permission.has
import com.peco2282.devcore.permission.permission
import com.peco2282.devcore.permission.registerPermissions
import com.peco2282.devcore.ranking.ranking
import com.peco2282.devcore.worldpool.worldPool
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.Plugin

/** Compile-checked examples for reusable server-side features. */
object ServerFeatures {
  val ADMIN = permission("testplugin.admin", PermissionDefault.OP) {
    description = "TestPlugin administration"
    child("testplugin.kit.starter")
  }
  val KIT_STARTER = permission("testplugin.kit.starter", PermissionDefault.TRUE)
  val names = ttlCache<UUID, String>(5.minutes, maxSize = 500)
  private val scores = ConcurrentHashMap<UUID, Long>()

  fun install(plugin: Plugin) {
    plugin.registerPermissions(KIT_STARTER, ADMIN)
    plugin.debugController().enable("modules")
    plugin.debugLogger("modules").timed("module setup") {
      plugin.kits().define("starter") {
        permission = KIT_STARTER.node
        cooldown = 1.days
        items {
          item(Material.STONE_SWORD)
          item(Material.COOKED_BEEF, 16)
        }
      }
    }
    plugin.ranking<UUID>(1.minutes) { scores.toMap() }
  }

  fun onJoin(plugin: Plugin, player: Player) {
    names.put(player.uniqueId, player.name)
    scores.putIfAbsent(player.uniqueId, 0)
    if (player.has(KIT_STARTER)) plugin.kits().give("starter", player)
  }

  fun claimDaily(plugin: Plugin, player: Player): Boolean =
    PersistentCooldowns(plugin).tryUse(player, "daily-reward", 1.days)

  fun createArenaPool(plugin: Plugin, template: File) =
    plugin.worldPool(template)
}
