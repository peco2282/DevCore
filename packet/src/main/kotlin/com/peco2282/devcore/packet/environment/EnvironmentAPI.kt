package com.peco2282.devcore.packet.environment

import org.bukkit.Location
import org.bukkit.entity.Player


/**
 * Hub interface for packet-based environment manipulation.
 *
 * Provides methods to send fake weather, time, biome, and world border
 * updates to individual players without affecting the actual server state.
 */
interface EnvironmentHub {
  /**
   * Sends a fake weather state to the player.
   *
   * @param player The target player.
   * @param rain Whether it should appear to be raining.
   * @param thunder Whether thunder should be active.
   */
  fun setFakeWeather(player: Player, rain: Boolean, thunder: Boolean)

  /**
   * Sends a fake time update to the player.
   *
   * @param player The target player.
   * @param time The time of day in ticks (0–24000).
   * @param locked Whether the time should be frozen.
   */
  fun setFakeTime(player: Player, time: Long, locked: Boolean = false)

  /**
   * Sends a fake biome override to the player for their current chunk.
   *
   * @param player The target player.
   * @param biomeKey The namespaced biome key (e.g. `"minecraft:desert"`).
   */
  fun setFakeBiome(player: Player, biomeKey: String)

  /**
   * Sends a fake world border configuration to the player.
   *
   * @param player The target player.
   * @param builder DSL block for configuring the fake world border.
   */
  fun setFakeWorldBorder(player: Player, builder: FakeWorldBorderBuilder.() -> Unit)

  /**
   * Resets the player's world border to the server's actual world border.
   *
   * @param player The target player.
   */
  fun resetWorldBorder(player: Player)

  /**
   * Sets the rain and thunder intensity levels for the player via packets.
   *
   * @param player The target player.
   * @param rainLevel The rain intensity (0.0–1.0).
   * @param thunderLevel The thunder intensity (0.0–1.0).
   */
  fun setWeatherLevel(player: Player, rainLevel: Float, thunderLevel: Float)

  /**
   * Sends a world border update packet to the player.
   *
   * @param player The target player.
   * @param builder DSL block for configuring the world border.
   */
  fun sendWorldBorder(player: Player, builder: WorldBorderBuilder.() -> Unit)
}

/**
 * DSL builder for configuring a world border sent to a player.
 */
interface WorldBorderBuilder {
  /** Sets the center of the world border. */
  fun center(location: Location)

  /** The X coordinate of the border center. */
  var centerX: Double
  /** The Z coordinate of the border center. */
  var centerZ: Double
  /** The diameter of the border. */
  var size: Double
  /** The damage per block for players outside the border. */
  var damageAmount: Double
  /** The distance from the border at which damage begins. */
  var damageBuffer: Double
  /** The distance from the border at which the warning appears. */
  var warningDistance: Int
  /** The time in seconds before the border reaches the player that the warning appears. */
  var warningTime: Int
}

/**
 * DSL builder for configuring a fake world border sent to a player.
 *
 * @property centerX The X coordinate of the border center.
 * @property centerZ The Z coordinate of the border center.
 * @property size The current border diameter in blocks.
 * @property oldSize The previous border diameter (used for lerp animation).
 * @property lerpTime The transition duration in milliseconds.
 * @property warningBlocks The distance from the border at which the warning overlay appears.
 * @property warningTime The time in seconds before the border reaches the player that the warning appears.
 */
interface FakeWorldBorderBuilder {
  var centerX: Double
  var centerZ: Double
  var size: Double
  var oldSize: Double
  var lerpTime: Long
  var warningBlocks: Int
  var warningTime: Int
}
