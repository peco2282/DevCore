package com.peco2282.devcore.packet.interact

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/** DSL marker annotation for the interact DSL scope. */
@DslMarker
annotation class PacketInteractDsl

/**
 * Hub interface for packet-based player interaction manipulation.
 *
 * Provides methods to place/remove fake blocks, lock inventory slots,
 * force held slot changes, and control the credits screen — all via packets
 * without modifying actual server state.
 */
interface InteractHub {
  /**
   * Sends a fake block placement packet to the player at the given location.
   *
   * @param player The target player.
   * @param location The location where the fake block appears.
   * @param material The material of the fake block.
   */
  fun placeFakeBlock(player: Player, location: Location, material: Material)

  /**
   * Sends a fake block removal packet to the player at the given location,
   * restoring the appearance of the original block.
   *
   * @param player The target player.
   * @param location The location of the fake block to remove.
   */
  fun removeFakeBlock(player: Player, location: Location)

  /**
   * Locks an inventory slot for the player by sending a fake item packet.
   *
   * @param player The target player.
   * @param slot The slot index to lock.
   * @param item The item to display in the locked slot, or `null` to show empty.
   */
  fun lockInventorySlot(player: Player, slot: Int, item: ItemStack?)

  /**
   * Forces the player's held item slot to the specified index via a packet.
   *
   * @param player The target player.
   * @param slot The hotbar slot index (0–8) to force.
   */
  fun forceHeldSlot(player: Player, slot: Int)

  /**
   * Shows the end credits screen to the player via a packet.
   *
   * @param player The target player.
   */
  fun showCredits(player: Player)

  /**
   * Hides the end credits screen for the player via a packet.
   *
   * @param player The target player.
   */
  fun hideCredits(player: Player)

  /**
   * Opens a sign editor for the player at the given location.
   *
   * @param player The target player.
   * @param location The location of the sign block.
   * @param front Whether to open the front face of the sign.
   */
  fun sendOpenSign(player: Player, location: Location, front: Boolean)

  /**
   * Sends fake block changes to the player using a [FakeBlockBuilder].
   *
   * @param player The target player.
   * @param builder DSL block for specifying fake block positions and materials.
   */
  fun sendFakeBlocks(player: Player, builder: FakeBlockBuilder.() -> Unit)
}

/**
 * DSL builder for specifying multiple fake block changes.
 */
interface FakeBlockBuilder {
  /** Sets a fake block at the given location. */
  fun set(location: Location, material: Material)
  /** Fills a region with fake blocks of the specified material. */
  fun fill(from: Location, to: Location, material: Material)
}
