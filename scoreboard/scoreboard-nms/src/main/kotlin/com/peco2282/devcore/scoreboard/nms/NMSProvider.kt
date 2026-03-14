package com.peco2282.devcore.scoreboard.nms

import com.peco2282.devcore.scoreboard.api.ScoreboardApi
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

object NMSProvider {
  fun init(plugin: Plugin) {
    val version = Bukkit.getMinecraftVersion()
    val factoryClass = when (version) {
      "1.21.4" -> "com.peco2282.devcore.scoreboard.nms.v1_21_4.NMSScoreboardFactory_v1_21_4"
      else -> {
        plugin.logger.warning("Unsupported Minecraft version for Scoreboard NMS: $version. Falling back to default if available.")
        null
      }
    }

    val factory = factoryClass?.let {
      try {
        Class.forName(it).getDeclaredConstructor().newInstance() as? ScoreboardNMSFactory
      } catch (e: Exception) {
        plugin.logger.warning("Failed to initialize Scoreboard NMS for version: $version. Error: ${e.message}")
        null
      }
    }

    if (factory != null) {
      ScoreboardApi.init(plugin, factory)
      plugin.logger.info("Scoreboard NMS initialized for version: $version")
    }
  }
}
