package com.peco2282.devcore.scoreboard.nms

import com.peco2282.devcore.scoreboard.api.ScoreboardApi
import com.peco2282.devcore.scoreboard.api.factory.ScoreboardNMSFactory
import com.peco2282.devcore.util.MinecraftVersion
import com.peco2282.devcore.util.versioned
import org.bukkit.plugin.Plugin

object NMSProvider {
  /** Per-version factories. Add a `v1_XX_X` submodule and a `since` line to support another version. */
  private val factories = versioned<ScoreboardNMSFactory>("Scoreboard NMS") {
    since("1.21.4", "com.peco2282.devcore.scoreboard.nms.v1_21_4.NMSScoreboardFactoryImpl")
    supportedBefore("1.22")
  }

  fun init(plugin: Plugin) {
    val version = MinecraftVersion.current()
    val factory = try {
      factories.load(version)
    } catch (e: IllegalStateException) {
      plugin.logger.warning("Scoreboard NMS unavailable on $version: ${e.message}. Falling back to default if available.")
      null
    }

    if (factory != null) {
      ScoreboardApi.init(plugin, factory)
      plugin.logger.info("Scoreboard NMS initialized for version: $version")
    }
  }
}
