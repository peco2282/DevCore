package com.peco2282.devcore.gui

import org.bukkit.inventory.ItemStack

@GuiDsl
class SlotCreator {
  internal val events: MutableList<GuiClickEvent.() -> Unit> = mutableListOf()
  internal var item: ItemStack = ItemStack.empty()
  internal var pickable: Boolean = true
  fun onClick(event: GuiClickEvent.() -> Unit): SlotCreator = apply {
    events.add(event)
  }

  fun icon(item: ItemStack): SlotCreator = apply {
    this.item = item
  }

  fun icon(material: org.bukkit.Material, amount: Int = 1, creator: ItemStack.() -> Unit = {}): SlotCreator = apply {
    this.item = ItemStack(material, amount)
    this.item.apply(creator)
  }

  fun name(name: net.kyori.adventure.text.Component?): SlotCreator = apply {
    item.editMeta { it.displayName(name) }
  }

  fun lore(lore: List<net.kyori.adventure.text.Component>?): SlotCreator = apply {
    item.editMeta { it.lore(lore) }
  }

  fun lore(vararg lore: net.kyori.adventure.text.Component): SlotCreator = lore(lore.toList())

  fun pickable(pickable: Boolean): SlotCreator = apply {
    this.pickable = pickable
  }

  fun pickable(): SlotCreator = pickable(true)

  fun keep(): SlotCreator = pickable(false)

}