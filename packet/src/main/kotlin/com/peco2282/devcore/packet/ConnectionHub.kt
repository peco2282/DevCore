package com.peco2282.devcore.packet

import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.entity.Player

/**
 * Hub interface for managing packet-level connections for players.
 */
interface ConnectionHub {
  /**
   * Injects a player into the Netty pipeline to begin packet interception.
   *
   * @param player The player to inject.
   */
  fun injectPlayer(player: Player)

  /**
   * Removes a player from the Netty pipeline, stopping packet interception.
   *
   * @param player The player to remove.
   */
  fun removePlayer(player: Player)

  /**
   * Sends a raw NMS packet object to the player.
   *
   * @param player The target player.
   * @param packet The NMS packet instance.
   */
  fun sendPacket(player: Player, packet: Any)

  /**
   * Gets the network settings (e.g. protocol version, latency) for a player.
   *
   * @param player The target player.
   */
  fun getNetworkSettings(player: Player): NetworkSettings

  /**
   * Gets a [CoroutineDispatcher] that executes tasks on the player's Netty event loop.
   *
   * @param player The target player.
   */
  fun getCoroutineDispatcher(player: Player): CoroutineDispatcher
}
