package com.peco2282.devcore.packet.environment

import com.peco2282.devcore.packet.PacketAPI
import org.bukkit.entity.Player

/** DSL marker annotation for the environment DSL scope. */
@DslMarker
annotation class PacketEnvironmentDsl

/**
 * DSL builder for sending fake environment effects to a [Player].
 *
 * Obtain an instance via the [Player.environment] extension function.
 *
 * @param player The target player for all operations in this builder.
 */
@PacketEnvironmentDsl
class EnvironmentBuilder(private val player: Player) {
  /**
   * Sends a fake weather state to the player.
   *
   * @param rain Whether it should appear to be raining.
   * @param thunder Whether thunder should be active.
   */
  fun weather(rain: Boolean, thunder: Boolean = false) {
    PacketAPI.setFakeWeather(player, rain, thunder)
  }

  /**
   * Sends a fake time update to the player.
   *
   * @param time The time of day in ticks (0–24000).
   * @param locked Whether the time should be frozen.
   */
  fun time(time: Long, locked: Boolean = false) {
    PacketAPI.setFakeTime(player, time, locked)
  }

  /**
   * Sends a fake biome override to the player.
   *
   * @param biomeKey The namespaced biome key (e.g. `"minecraft:desert"`).
   */
  fun biome(biomeKey: String) {
    PacketAPI.setFakeBiome(player, biomeKey)
  }

  /**
   * Sends a fake world border configuration to the player.
   *
   * @param builder DSL block for configuring the fake world border.
   */
  fun worldBorder(builder: FakeWorldBorderBuilder.() -> Unit) {
    PacketAPI.setFakeWorldBorder(player, builder)
  }

  /** Resets the player's world border to the server's actual world border. */
  fun resetWorldBorder() {
    PacketAPI.resetWorldBorder(player)
  }

  /**
   * Sets the rain and thunder intensity levels for the player.
   *
   * @param rainLevel The rain intensity (0.0–1.0).
   * @param thunderLevel The thunder intensity (0.0–1.0).
   */
  fun weatherLevel(rainLevel: Float, thunderLevel: Float = 0f) {
    PacketAPI.setWeatherLevel(player, rainLevel, thunderLevel)
  }
}

/**
 * Entry point for the environment DSL. Applies [action] to an [EnvironmentBuilder] for this player.
 *
 * @param action DSL block for sending fake environment effects.
 */
fun Player.environment(action: EnvironmentBuilder.() -> Unit) {
  EnvironmentBuilder(this).apply(action)
}
