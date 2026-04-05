package com.peco2282.devcore.packet.inventory

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/** DSL marker annotation for the inventory management DSL scope. */
@DslMarker
annotation class PacketInventoryDsl

/**
 * Hub interface for packet-based inventory and GUI manipulation.
 *
 * Provides methods for faking inventory titles, item slots, and
 * progress bars in container interfaces.
 */
interface InventoryHub {
  /**
   * Updates the title of the player's currently open inventory via a packet.
   *
   * @param player The target player.
   * @param title The new inventory title.
   */
  fun updateInventoryTitle(player: Player, title: String)

  /**
   * Fakes an item in a specific inventory slot for the player.
   *
   * @param player The target player.
   * @param windowId The ID of the inventory window.
   * @param slot The slot index.
   * @param item The fake item to display.
   */
  fun fakeItemSlot(player: Player, windowId: Int, slot: Int, item: ItemStack)

  /**
   * Fakes the progress bars (cook time, fuel) in a furnace GUI for the player.
   *
   * @param player The target player.
   * @param cookProgress The cooking progress (0–max).
   * @param fuelProgress The fuel remaining (0–max).
   */
  fun fakeFurnaceProgress(player: Player, cookProgress: Int, fuelProgress: Int)

  /**
   * Sets a fake item cooldown for the player.
   *
   * @param player The target player.
   * @param item The item material to apply the cooldown to.
   * @param ticks The duration of the cooldown in ticks.
   */
  fun setItemCooldown(player: Player, item: org.bukkit.Material, ticks: Int)
}
