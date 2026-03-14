package com.peco2282.devcore.scoreboard.nms

import com.peco2282.devcore.scoreboard.api.ScoreboardApi
import com.peco2282.devcore.scoreboard.nms.v1_21_4.NMSScoreboardFactory_v1_21_4
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

object NMSProvider {
  fun init(plugin: Plugin) {
    val version = Bukkit.getMinecraftVersion()
    val factory = when (version) {
      "1.21.4" -> NMSScoreboardFactory_v1_21_4()
      else -> {
        plugin.logger.warning("Unsupported Minecraft version for Scoreboard NMS: $version. Falling back to default if available.")
        return
      }
    }
    ScoreboardApi.init(plugin, factory)
    plugin.logger.info("Scoreboard NMS initialized for version: $version")
  }
}
