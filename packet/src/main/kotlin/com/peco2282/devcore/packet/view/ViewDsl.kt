package com.peco2282.devcore.packet.view

import com.peco2282.devcore.packet.PacketAPI
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

/**
 * DSL builder for sending packet-based visual perspective and entity appearance effects to a [Player].
 *
 * Obtain an instance via the [Player.view] extension function.
 *
 * @param player The target player for all operations in this builder.
 */
@PacketViewDsl
class ViewBuilder(private val player: Player) {
  /**
   * Sets the player's camera to spectate the specified entity.
   *
   * @param entityId The entity ID to attach the camera to.
   */
  fun camera(entityId: Int) {
    PacketAPI.setCameraEntity(player, entityId)
  }

  /** Resets the player's camera back to their own perspective. */
  fun resetCamera() {
    PacketAPI.resetCamera(player)
  }

  /**
   * Sets the glowing visual effect on an entity for the player.
   *
   * @param entityId The entity to update.
   * @param glowing Whether the entity should appear glowing.
   */
  fun glow(entityId: Int, glowing: Boolean = true) {
    PacketAPI.setEntityGlowing(player, entityId, glowing)
  }

  /**
   * Makes an entity appear as a different type for the player without changing it server-side.
   *
   * @param entityId The entity to transform.
   * @param type The entity type to display.
   */
  fun transformType(entityId: Int, type: EntityType) {
    PacketAPI.transformEntityType(player, entityId, type)
  }

  /**
   * Sets the display scale of an entity for the player.
   *
   * @param entityId The entity to scale.
   * @param scale The scale factor (1.0 = normal size).
   */
  fun scale(entityId: Int, scale: Float) {
    PacketAPI.setEntityScale(player, entityId, scale)
  }

  /**
   * Sets whether an entity appears upside-down for the player.
   *
   * @param entityId The entity to update.
   * @param upsideDown Whether the entity should appear upside-down.
   */
  fun upsideDown(entityId: Int, upsideDown: Boolean = true) {
    PacketAPI.setEntityUpsideDown(player, entityId, upsideDown)
  }
}

/**
 * Entry point for the view DSL. Applies [action] to a [ViewBuilder] for this player.
 *
 * @param action DSL block for sending packet-based visual perspective and entity appearance effects.
 */
fun Player.view(action: ViewBuilder.() -> Unit) {
  ViewBuilder(this).apply(action)
}
