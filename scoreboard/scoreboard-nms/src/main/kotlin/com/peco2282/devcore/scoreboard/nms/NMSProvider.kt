package com.peco2282.devcore.scoreboard.nms

import com.peco2282.devcore.scoreboard.api.ScoreboardApi
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

object NMSProvider {
    fun init(plugin: Plugin) {
        val version = Bukkit.getMinecraftVersion()
        val className = when (version) {
            "1.21.4" -> "com.peco2282.devcore.scoreboard.nms.v1_21_4.NMSScoreboardFactory_v1_21_4"
            else -> {
                plugin.logger.warning("Unsupported Minecraft version for Scoreboard NMS: $version. Falling back to default if available.")
                return
            }
        }
        
        try {
            val clazz = Class.forName(className)
            val factory = clazz.getDeclaredConstructor().newInstance() as ScoreboardNMSFactory
            ScoreboardApi.init(plugin, factory)
            plugin.logger.info("Scoreboard NMS initialized for version: $version")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to initialize Scoreboard NMS for version $version: ${e.message}")
            e.printStackTrace()
        }
    }
}
