package com.peco2282.devcore.packet.vfx

import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/** DSL marker annotation for the visual effects DSL scope. */
@DslMarker
annotation class PacketVfxDsl

/**
 * Hub interface for packet-based visual and audio effects.
 *
 * Provides methods to send block crack animations, entity fire states,
 * fake explosions, lightning strikes, equipment faking, and positional sounds
 * to individual players without affecting the actual server state.
 */
interface VfxHub {
  /**
   * Sends a block crack (mining progress) animation to the player.
   *
   * @param player The target player.
   * @param location The location of the block being cracked.
   * @param stage The crack stage (0–9, where 9 is fully cracked; -1 resets).
   */
  fun setBlockCrack(player: Player, location: Location, stage: Int)

  /**
   * Sends a fake equipment packet to the player for the specified entity slot.
   *
   * @param player The target player.
   * @param entityId The entity whose equipment is faked.
   * @param slot The equipment slot to modify.
   * @param item The item to display in the slot.
   */
  fun fakeEquipment(player: Player, entityId: Int, slot: EquipmentSlot, item: ItemStack)

  /**
   * Sets the on-fire visual state of an entity for the player via a metadata packet.
   *
   * @param player The target player.
   * @param entityId The entity to update.
   * @param onFire Whether the entity should appear to be on fire.
   */
  fun setEntityOnFire(player: Player, entityId: Int, onFire: Boolean)

  /**
   * Sends a fake explosion effect to the player at the given location.
   *
   * @param player The target player.
   * @param location The center of the explosion.
   * @param power The explosion power (affects visual radius).
   */
  fun fakeExplosion(player: Player, location: Location, power: Float)

  /**
   * Sends a fake lightning strike effect to the player at the given location.
   *
   * @param player The target player.
   * @param location The location of the lightning strike.
   */
  fun fakeLightning(player: Player, location: Location)

  /**
   * Plays a sound at a specific location for the player only, without affecting other players.
   *
   * @param player The target player.
   * @param sound The sound to play.
   * @param location The location from which the sound originates.
   * @param volume The volume level.
   * @param pitch The pitch level.
   */
  fun localSound(player: Player, sound: Sound, location: Location, volume: Float = 1f, pitch: Float = 1f)
}
