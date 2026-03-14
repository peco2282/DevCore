package com.peco2282.devcore.gui

import net.kyori.adventure.text.Component
import org.bukkit.inventory.Inventory

/**
 * Creates a simple GUI (Inventory) using a DSL.
 *
 * Example usage:
 * ```kotlin
 * val inventory = gui(Component.text("Simple GUI"), 3) {
 *     slot(SLOT_2_5) {
 *         icon(Material.APPLE)
 *         onClick { player.sendMessage("Clicked!") }
 *     }
 * }
 * player.openInventory(inventory)
 * ```
 *
 * @param title The title of the GUI.
 * @param rows The number of rows (1-6).
 * @param creator The builder block.
 * @return A new Bukkit [Inventory].
 */
fun gui(title: Component, rows: Int = 6, creator: GuiCreator.() -> Unit): Inventory {
  val builder = GuiCreator(rows)
  builder.title = title
  builder.creator()
  return builder.create()
}

/**
 * Creates a declarative [Gui] instance.
 *
 * @param rows The number of rows (1-6).
 * @param title The title of the GUI.
 * @param builder The builder block for the [GuiContext].
 * @return A new [Gui] instance.
 */
fun inventory(rows: Int = 3, title: Component, builder: GuiContext.() -> Unit): Gui {
  return object : GuiContext(rows) {
    override fun build() {
      title(title)
      this.builder()
    }
  }
}

/**
 * Includes another builder block as a template.
 */
fun GuiCreator.template(template: GuiCreator.() -> Unit) {
  this.template()
}

/**
 * Includes another builder block as a template.
 */
fun GuiContext.template(template: GuiContext.() -> Unit) {
  this.template()
}

/**
 * Fills all slots in the GUI with a material.
 *
 * @param material The material to fill with.
 * @param pickable Whether the items are pickable.
 * @param creator Additional configuration for each slot.
 */
fun GuiCreator.fill(material: org.bukkit.Material, pickable: Boolean = false, creator: SlotCreator.() -> Unit = {}) {
  for (i in 0 until (rows * 9)) {
    slot(i) {
      icon(material)
      pickable(pickable)
      creator()
    }
  }
}