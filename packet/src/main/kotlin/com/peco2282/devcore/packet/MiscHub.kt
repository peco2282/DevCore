package com.peco2282.devcore.packet

import io.netty.buffer.ByteBuf
import org.bukkit.entity.Player

/**
 * Hub interface for miscellaneous packet-based operations.
 */
interface MiscHub {
  /**
   * Sends a fake statistic update to the player via a packet.
   *
   * @param player The target player.
   * @param statistic The statistic to update.
   * @param value The new value for the statistic.
   */
  fun fakeStatistic(player: Player, statistic: org.bukkit.Statistic, value: Int)

  /**
   * Sends a fake experience bar update to the player via a packet.
   *
   * @param player The target player.
   * @param bar The experience progress (0.0–1.0).
   * @param level The experience level.
   * @param experience Total experience.
   */
  fun fakeExperienceBar(player: Player, bar: Float, level: Int, experience: Int)

  /**
   * Shows a fake death screen to the player via a packet.
   *
   * @param player The target player.
   * @param message The death message.
   */
  fun showFakeDeathScreen(player: Player, message: String)

  /**
   * Sends a raw packet via a custom plugin channel.
   *
   * @param player The target player.
   * @param channel The plugin channel name.
   * @param buf The raw byte buffer containing the packet data.
   */
  fun sendRawPacket(player: Player, channel: String, buf: ByteBuf)
}
