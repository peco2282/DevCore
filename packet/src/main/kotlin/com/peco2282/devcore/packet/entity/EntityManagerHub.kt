package com.peco2282.devcore.packet.entity

import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import com.peco2282.devcore.packet.FakeEntityBuilder
import com.peco2282.devcore.packet.MetadataBuilder

/** DSL marker annotation for the entity management DSL scope. */
@DslMarker
annotation class PacketEntityDsl

/**
 * Hub interface for packet-based entity management.
 *
 * Provides methods for creating fake entities, managing entity visibility,
 * and faking animations and metadata updates for existing entities.
 */
interface EntityManagerHub {
  /**
   * Creates a new [FakeEntityBuilder] for faking an entity to the player.
   *
   * @param player The target player.
   * @param type The type of entity to create.
   * @param location The initial location.
   */
  fun createFakeEntityBuilder(player: Player, type: EntityType, location: Location): FakeEntityBuilder

  /**
   * Sends a raw metadata update for an entity to the player.
   *
   * @param player The target player.
   * @param entityId The entity ID.
   * @param data The raw metadata object or map (implementation dependent).
   */
  fun sendMetadata(player: Player, entityId: Int, data: MetadataBuilder.() -> Unit)

  /**
   * Hides an entity from the player via a packet.
   *
   * @param player The target player.
   * @param entityId The entity ID to hide.
   */
  fun hideEntity(player: Player, entityId: Int)

  /**
   * Shows a previously hidden entity to the player.
   *
   * @param player The target player.
   * @param entityId The entity ID to show.
   */
  fun showEntity(player: Player, entityId: Int)

  /**
   * Fakes the display name of a player in the tab list or above their head.
   *
   * @param player The target player.
   * @param target The player whose name is being faked.
   * @param name The fake name to display.
   */
  fun fakePlayerName(player: Player, target: Player, name: String)

  /**
   * Sends an eating animation packet for an entity to the player.
   *
   * @param player The target player.
   * @param entityId The entity performing the animation.
   */
  fun setEatingAnimation(player: Player, entityId: Int)

  /**
   * Sends a bow/crossbow charging animation packet for an entity to the player.
   *
   * @param player The target player.
   * @param entityId The entity performing the animation.
   */
  fun setBowAnimation(player: Player, entityId: Int)

  /**
   * Sends a shield/sword guard pose packet for an entity to the player.
   *
   * @param player The target player.
   * @param entityId The entity performing the animation.
   */
  fun setGuardPose(player: Player, entityId: Int)

  /**
   * Sends a sleep animation packet for an entity at the given location to the player.
   *
   * @param player The target player.
   * @param entityId The entity to put to sleep.
   * @param location The location of the bed.
   */
  fun setSleepAnimation(player: Player, entityId: Int, location: Location)

  /**
   * Sets the velocity/motion of an entity for the player via a packet.
   *
   * @param player The target player.
   * @param entityId The entity to update.
   * @param velocity The velocity vector.
   */
  fun setEntityMotion(player: Player, entityId: Int, velocity: Vector)
}
