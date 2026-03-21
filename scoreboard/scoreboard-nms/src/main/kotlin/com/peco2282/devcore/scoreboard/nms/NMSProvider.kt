package com.peco2282.devcore.scoreboard.nms

import com.peco2282.devcore.scoreboard.api.ScoreboardApi
import com.peco2282.devcore.scoreboard.api.factory.ScoreboardNMSFactory
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

object NMSProvider {
  private fun factoryClass(version: String): String {
    val v = version.replace(".", "_")
    return "com.peco2282.devcore.scoreboard.nms.v$v.NMSScoreboardFactoryImpl"
  }

  fun init(plugin: Plugin) {
    val version = Bukkit.getMinecraftVersion()
    val factoryClass = when (version) {
      "1.20.2" -> factoryClass("1.20.2")
      in "1.20.3".."1.21.3" -> factoryClass("1.20.3")
      in "1.21.4"..<"1.22" -> factoryClass("1.21.4")
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
