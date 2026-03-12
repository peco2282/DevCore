package com.peco2282.devcore.gui

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory

@GuiDsl
class GuiCreator internal constructor(val rows: Int) {
  var title: Component = Component.empty()

  init {
    require(rows in 1..6) { "rows must be between 1 and 6" }
  }

  private val slots: Int2ObjectMap<SlotCreator> = Int2ObjectLinkedOpenHashMap()

  fun getSlots(): Map<Int, SlotCreator> = slots

  fun slot(slot: Int) {
    slots.computeIfAbsent(slot) { SlotCreator() }
  }

  fun slot(slot: Int, creator: SlotCreator.() -> Unit) {
    slots.getOrPut(slot) { SlotCreator() }.apply(creator)
  }

  fun slot(x: Int, y: Int, creator: SlotCreator.() -> Unit) {
    slot((x - 1) * 9 + (y - 1), creator)
  }

  fun slot(slot: Slot, creator: SlotCreator.() -> Unit) = slot(slot.slot(), creator)

  fun fillBorder(material: Material, pickable: Boolean = false, creator: SlotCreator.() -> Unit = {}) {
    for (i in 0 until (rows * 9)) {
      val x = i / 9
      val y = i % 9
      if (x == 0 || x == rows - 1 || y == 0 || y == 8) {
        slot(i) {
          icon(material)
          pickable(pickable)
          creator()
        }
      }
    }
  }

  fun fillRect(
    x1: Int,
    y1: Int,
    x2: Int,
    y2: Int,
    material: Material,
    pickable: Boolean = false,
    creator: SlotCreator.() -> Unit = {}
  ) {
    val minX = minOf(x1, x2)
    val maxX = maxOf(x1, x2)
    val minY = minOf(y1, y2)
    val maxY = maxOf(y1, y2)
    for (x in minX..maxX) {
      for (y in minY..maxY) {
        if (x >= rows || y > 9 || y < 1) continue
        slot(x, y) {
          icon(material)
          pickable(pickable)
          creator()
        }
      }
    }
  }

  fun fillRect(
    s1: Slot,
    s2: Slot,
    material: Material,
    pickable: Boolean = false,
    creator: SlotCreator.() -> Unit = {}
  ) =
    fillRect(s1.x, s1.y, s2.x, s2.y, material, pickable, creator)

  fun create(): Inventory {
    val inventory = Bukkit.createInventory(null, rows * 9, title)
    slots.forEach { (slot, creator) ->
      inventory.setItem(slot, creator.item)
    }
    return inventory
  }
}
