package com.peco2282.devcore.packet.interact

import com.peco2282.devcore.packet.PacketAPI
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * DSL builder for sending packet-based interaction effects to a [Player].
 *
 * Obtain an instance via the [Player.interact] extension function.
 *
 * @param player The target player for all operations in this builder.
 */
@PacketInteractDsl
class InteractBuilder(private val player: Player) {
  /**
   * Sends a fake block placement packet to the player.
   *
   * @param location The location where the fake block appears.
   * @param material The material of the fake block.
   */
  fun fakeBlock(location: Location, material: Material) {
    PacketAPI.placeFakeBlock(player, location, material)
  }

  /**
   * Sends a fake block removal packet to the player, restoring the original block appearance.
   *
   * @param location The location of the fake block to remove.
   */
  fun removeFakeBlock(location: Location) {
    PacketAPI.removeFakeBlock(player, location)
  }

  /**
   * Locks an inventory slot for the player by sending a fake item packet.
   *
   * @param slot The slot index to lock.
   * @param item The item to display in the locked slot, or `null` to show empty.
   */
  fun lockSlot(slot: Int, item: ItemStack?) {
    PacketAPI.lockInventorySlot(player, slot, item)
  }

  /**
   * Forces the player's held item slot to the specified index via a packet.
   *
   * @param slot The hotbar slot index (0–8) to force.
   */
  fun forceHeldSlot(slot: Int) {
    PacketAPI.forceHeldSlot(player, slot)
  }

  /** Shows the end credits screen to the player via a packet. */
  fun showCredits() {
    PacketAPI.showCredits(player)
  }

  /** Hides the end credits screen for the player via a packet. */
  fun hideCredits() {
    PacketAPI.hideCredits(player)
  }
}

/**
 * Entry point for the interact DSL. Applies [action] to an [InteractBuilder] for this player.
 *
 * @param action DSL block for sending packet-based interaction effects.
 */
fun Player.interact(action: InteractBuilder.() -> Unit) {
  InteractBuilder(this).apply(action)
}
