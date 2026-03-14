package com.peco2282.devcore.gui

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory

/**
 * A creator class for building the initial state of a GUI.
 *
 * @param rows The number of rows in the GUI.
 */
@GuiDsl
class GuiCreator internal constructor(val rows: Int) {
  /**
   * The title of the GUI.
   */
  var title: Component = Component.empty()

  init {
    require(rows in 1..6) { "rows must be between 1 and 6" }
  }

  private val slots: Int2ObjectMap<SlotCreator> = Int2ObjectLinkedOpenHashMap()

  /**
   * Returns a map of configured slots.
   */
  fun getSlots(): Map<Int, SlotCreator> = slots

  /**
   * Configures a slot at the specified index.
   *
   * @param slot The raw index of the slot (0-indexed).
   */
  fun slot(slot: Int) {
    slots.computeIfAbsent(slot) { SlotCreator() }
  }

  /**
   * Configures a slot at the specified index with a builder.
   *
   * @param slot The raw index of the slot (0-indexed).
   * @param creator The builder block for the slot.
   */
  fun slot(slot: Int, creator: SlotCreator.() -> Unit) {
    slots.getOrPut(slot) { SlotCreator() }.apply(creator)
  }

  /**
   * Configures a slot by its X and Y coordinates.
   *
   * @param x The row index (1-indexed).
   * @param y The column index (1-indexed).
   * @param creator The builder block for the slot.
   */
  fun slot(x: Int, y: Int, creator: SlotCreator.() -> Unit) {
    slot((x - 1) * 9 + (y - 1), creator)
  }

  /**
   * Configures a slot using a [Slot] instance.
   *
   * @param slot The slot instance.
   * @param creator The builder block for the slot.
   */
  fun slot(slot: Slot, creator: SlotCreator.() -> Unit) = slot(slot.slot(), creator)

  /**
   * Fills the border of the GUI with a material.
   *
   * @param material The material to fill with.
   * @param pickable Whether the border items are pickable.
   * @param creator Additional configuration for each slot in the border.
   */
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

  /**
   * Fills a rectangular area with a material.
   *
   * @param x1 Starting row index.
   * @param y1 Starting column index.
   * @param x2 Ending row index.
   * @param y2 Ending column index.
   * @param material The material to fill with.
   * @param pickable Whether the items are pickable.
   * @param creator Additional configuration for each slot in the area.
   */
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

  /**
   * Fills a rectangular area defined by two slots with a material.
   */
  fun fillRect(
    s1: Slot,
    s2: Slot,
    material: Material,
    pickable: Boolean = false,
    creator: SlotCreator.() -> Unit = {}
  ) =
    fillRect(s1.x, s1.y, s2.x, s2.y, material, pickable, creator)

  /**
   * Creates a Bukkit inventory based on the current configuration.
   */
  fun create(): Inventory {
    val inventory = Bukkit.createInventory(null, rows * 9, title)
    slots.forEach { (slot, creator) ->
      inventory.setItem(slot, creator.item)
    }
    return inventory
  }
}
