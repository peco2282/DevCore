package com.peco2282.devcore.gui

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

/**
 * Utility for getting chat input from a player.
 *
 * @param plugin The plugin instance.
 * @param timeoutTicks The timeout in ticks (optional).
 * @param onInput The callback for when input is received.
 * @param onCancel The callback for when input is cancelled or timed out.
 */
fun Player.awaitChatInput(
  plugin: Plugin,
  timeoutTicks: Long? = null,
  onInput: (Component) -> Unit,
  onCancel: () -> Unit = {}
) {
  val listener = object : Listener {
    @EventHandler
    fun onChat(event: AsyncChatEvent) {
      if (event.player.uniqueId != uniqueId) return
      event.isCancelled = true
      val message = event.originalMessage()

      // Execute on main thread
      plugin.server.scheduler.runTask(plugin, Runnable {
        onInput(message)
        AsyncChatEvent.getHandlerList().unregister(this)
      })
    }
  }

  plugin.server.pluginManager.registerEvents(listener, plugin)

  if (timeoutTicks != null) {
    plugin.server.scheduler.runTaskLater(plugin, Runnable {
      AsyncChatEvent.getHandlerList().unregister(listener)
      onCancel()
    }, timeoutTicks)
  }
}

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
fun GuiCreator.fill(material: Material, pickable: Boolean = false, creator: SlotCreator.() -> Unit = {}) {
  for (i in 0 until (rows * 9)) {
    slot(i) {
      icon(material)
      pickable(pickable)
      creator()
    }
  }
}