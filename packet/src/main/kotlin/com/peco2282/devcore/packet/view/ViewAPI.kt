package com.peco2282.devcore.packet.view

import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

/** DSL marker annotation for the view DSL scope. */
@DslMarker
annotation class PacketViewDsl

/**
 * Hub interface for packet-based visual perspective and entity appearance manipulation.
 *
 * Provides methods to control the player's camera, toggle entity glowing,
 * transform entity types, and adjust entity scale and orientation — all via
 * packets without modifying actual server state.
 */
interface ViewHub {
  /**
   * Sets the player's camera to spectate the specified entity.
   *
   * @param player The target player.
   * @param entityId The entity ID to attach the camera to.
   */
  fun setCameraEntity(player: Player, entityId: Int)

  /**
   * Resets the player's camera back to their own perspective.
   *
   * @param player The target player.
   */
  fun resetCamera(player: Player)

  /**
   * Sets the glowing visual effect on an entity for the player via a metadata packet.
   *
   * @param player The target player.
   * @param entityId The entity to update.
   * @param glowing Whether the entity should appear glowing.
   */
  fun setEntityGlowing(player: Player, entityId: Int, glowing: Boolean)

  /**
   * Sends a fake entity type transformation to the player, making the entity appear
   * as a different type without changing it server-side.
   *
   * @param player The target player.
   * @param entityId The entity to transform.
   * @param type The entity type to display.
   */
  fun transformEntityType(player: Player, entityId: Int, type: EntityType)

  /**
   * Sets the display scale of an entity for the player via a metadata packet.
   *
   * @param player The target player.
   * @param entityId The entity to scale.
   * @param scale The scale factor (1.0 = normal size).
   */
  fun setEntityScale(player: Player, entityId: Int, scale: Float)

  /**
   * Sets whether an entity appears upside-down for the player via a metadata packet.
   *
   * @param player The target player.
   * @param entityId The entity to update.
   * @param upsideDown Whether the entity should appear upside-down.
   */
  fun setEntityUpsideDown(player: Player, entityId: Int, upsideDown: Boolean)

  /**
   * Sends a title and subtitle to the player via packets.
   *
   * @param player The target player.
   * @param title The main title text.
   * @param subtitle The subtitle text.
   * @param fadeIn Fade-in duration in ticks.
   * @param stay Display duration in ticks.
   * @param fadeOut Fade-out duration in ticks.
   */
  fun sendTitle(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int)

  /**
   * Sends an action bar message to the player via a packet.
   *
   * @param player The target player.
   * @param message The action bar text.
   */
  fun sendActionBar(player: Player, message: String)

  /**
   * Sets the player's camera to the specified entity.
   *
   * @param player The target player.
   * @param entityId The entity ID to attach the camera to.
   */
  fun sendCamera(player: Player, entityId: Int)

  /**
   * Sends a fake sky/fog color to the player (via biome hack or similar).
   *
   * @param player The target player.
   * @param color The ARGB color integer.
   */
  fun setFakeSkyColor(player: Player, color: Int)
}
