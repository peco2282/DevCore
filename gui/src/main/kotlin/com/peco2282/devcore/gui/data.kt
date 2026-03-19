package com.peco2282.devcore.gui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * Context for GUI click events.
 *
 * @property player The player who clicked.
 * @property slot The index of the clicked slot.
 * @property bukkitEvent The underlying Bukkit [InventoryClickEvent].
 */
data class GuiClickEvent(
  val player: Player,
  val slot: Int,
  val bukkitEvent: InventoryClickEvent
) {
  /**
   * Returns the item that was clicked, if any.
   */
  val currentItem: ItemStack? get() = bukkitEvent.currentItem
}
