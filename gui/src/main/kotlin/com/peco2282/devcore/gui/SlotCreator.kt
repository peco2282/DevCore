package com.peco2282.devcore.gui

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * A builder class for configuring individual slots in a GUI.
 */
@GuiDsl
class SlotCreator {
  internal val events: MutableList<GuiClickEvent.() -> Unit> = mutableListOf()
  internal var item: ItemStack = ItemStack.empty()
  internal var pickable: Boolean = true

  /**
   * Registers a click event handler for this slot.
   */
  fun onClick(event: GuiClickEvent.() -> Unit): SlotCreator = apply {
    events.add(event)
  }

  /**
   * Sets the icon (item) for this slot.
   */
  fun icon(item: ItemStack): SlotCreator = apply {
    this.item = item
  }

  /**
   * Sets the icon (item) for this slot using material and amount.
   *
   * @param material The material of the item.
   * @param amount The amount of the item (default: 1).
   * @param creator Additional configuration for the [ItemStack].
   */
  fun icon(material: Material, amount: Int = 1, creator: ItemStack.() -> Unit = {}): SlotCreator = apply {
    this.item = ItemStack(material, amount)
    this.item.apply(creator)
  }

  /**
   * Sets the display name of the item in this slot.
   */
  fun name(name: Component?): SlotCreator = apply {
    val meta = item.itemMeta ?: return@apply
    meta.displayName(name)
    item.itemMeta = meta
  }

  /**
   * Sets the lore of the item in this slot.
   */
  fun lore(lore: List<Component>?): SlotCreator = apply {
    val meta = item.itemMeta ?: return@apply
    meta.lore(lore)
    item.itemMeta = meta
  }

  /**
   * Sets the lore of the item in this slot using varargs.
   */
  fun lore(vararg lore: Component): SlotCreator = lore(lore.toList())

  /**
   * Sets whether the item in this slot can be picked up by the player.
   */
  fun pickable(pickable: Boolean): SlotCreator = apply {
    this.pickable = pickable
  }

  /**
   * Makes the item in this slot pickable.
   */
  fun pickable(): SlotCreator = pickable(true)

  /**
   * Makes the item in this slot unpickable (it will remain in the inventory).
   */
  fun keep(): SlotCreator = pickable(false)
}