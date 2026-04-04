package com.peco2282.devcore.packet.vfx

import com.peco2282.devcore.packet.PacketAPI
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * DSL builder for sending packet-based visual and audio effects to a [Player].
 *
 * Obtain an instance via the [Player.vfx] extension function.
 *
 * @param player The target player for all operations in this builder.
 */
@PacketVfxDsl
class VfxBuilder(private val player: Player) {
  /**
   * Sends a block crack (mining progress) animation to the player.
   *
   * @param location The location of the block being cracked.
   * @param stage The crack stage (0–9, where 9 is fully cracked; -1 resets).
   */
  fun blockCrack(location: Location, stage: Int) {
    PacketAPI.setBlockCrack(player, location, stage)
  }

  /**
   * Sends a fake equipment packet to the player for the specified entity slot.
   *
   * @param entityId The entity whose equipment is faked.
   * @param slot The equipment slot to modify.
   * @param item The item to display in the slot.
   */
  fun fakeEquipment(entityId: Int, slot: EquipmentSlot, item: ItemStack) {
    PacketAPI.fakeEquipment(player, entityId, slot, item)
  }

  /**
   * Sets the on-fire visual state of an entity for the player.
   *
   * @param entityId The entity to update.
   * @param onFire Whether the entity should appear to be on fire.
   */
  fun onFire(entityId: Int, onFire: Boolean = true) {
    PacketAPI.setEntityOnFire(player, entityId, onFire)
  }

  /**
   * Sends a fake explosion effect to the player at the given location.
   *
   * @param location The center of the explosion.
   * @param power The explosion power (affects visual radius).
   */
  fun explosion(location: Location, power: Float = 1f) {
    PacketAPI.fakeExplosion(player, location, power)
  }

  /**
   * Sends a fake lightning strike effect to the player at the given location.
   *
   * @param location The location of the lightning strike.
   */
  fun lightning(location: Location) {
    PacketAPI.fakeLightning(player, location)
  }

  /**
   * Plays a sound at a specific location for the player only.
   *
   * @param sound The sound to play.
   * @param location The location from which the sound originates.
   * @param volume The volume level.
   * @param pitch The pitch level.
   */
  fun localSound(sound: Sound, location: Location, volume: Float = 1f, pitch: Float = 1f) {
    PacketAPI.localSound(player, sound, location, volume, pitch)
  }
}

/**
 * Entry point for the visual effects DSL. Applies [action] to a [VfxBuilder] for this player.
 *
 * @param action DSL block for sending packet-based visual and audio effects.
 */
fun Player.vfx(action: VfxBuilder.() -> Unit) {
  VfxBuilder(this).apply(action)
}
