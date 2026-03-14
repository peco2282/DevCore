package com.peco2282.devcore.gui

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.Plugin

/**
 * Internal inventory holder for [Gui].
 */
internal class GuiHolder(val gui: Gui) : InventoryHolder {
  private var inventory: Inventory? = null
  private val viewers = mutableSetOf<Player>()

  /**
   * Adds a viewer to the GUI.
   */
  fun addViewer(player: Player) {
    viewers.add(player)
  }

  /**
   * Removes a viewer from the GUI.
   */
  fun removeViewer(player: Player) {
    viewers.remove(player)
  }

  /**
   * Returns the set of players currently viewing the GUI.
   */
  fun getViewers(): Set<Player> = viewers

  /**
   * Sets the underlying Bukkit inventory.
   */
  fun setInventory(inventory: Inventory) {
    this.inventory = inventory
  }

  /**
   * Returns the underlying Bukkit inventory.
   *
   * @throws IllegalStateException If the inventory has not been initialized.
   */
  override fun getInventory(): Inventory {
    return inventory ?: throw IllegalStateException("Inventory not initialized")
  }
}

/**
 * Base class for all GUIs.
 *
 * @param rows The number of rows in the GUI (1-6).
 */
abstract class Gui(val rows: Int) {
  internal val holder = GuiHolder(this)

  /**
   * The underlying Bukkit inventory managed by this GUI.
   */
  lateinit var inventory: Inventory
    private set

  init {
    require(rows in 1..6) { "rows must be between 1 and 6" }
  }

  /**
   * Builds the GUI using the provided [GuiCreator].
   *
   * @param creator The creator instance used to define slots and items.
   */
  abstract fun build(creator: GuiCreator)

  /**
   * Generates or updates the inventory based on the current configuration.
   * This should be called whenever the GUI's state changes.
   */
  fun update() {
    if (this is GuiContext) {
      resetStateCounter()
    }
    val creator = GuiCreator(rows)
    build(creator)

    val newSlots = creator.getSlots()
    val viewers = holder.getViewers().toList()

    if (::inventory.isInitialized && viewers.isNotEmpty()) {
      val firstViewer = viewers.first()
      // タイトルが変わった場合はインベントリを再作成
      if (creator.title != firstViewer.openInventory.title()) {
        inventory = Bukkit.createInventory(holder, rows * 9, creator.title)
        holder.setInventory(inventory)
        // インベントリの中身を全て設定
        newSlots.forEach { (slot, slotCreator) ->
          inventory.setItem(slot, slotCreator.item)
        }
        viewers.forEach { it.openInventory(inventory) }
        this.currentSlots = newSlots.toMap()
        return
      }
    }

    if (!::inventory.isInitialized) {
      inventory = Bukkit.createInventory(holder, rows * 9, creator.title)
      holder.setInventory(inventory)
      newSlots.forEach { (slot, slotCreator) ->
        inventory.setItem(slot, slotCreator.item)
      }
    } else {
      // 🔥 差分更新: 変更があったスロットのみを更新する
      // 以前のスロットと比較して、アイテムが異なる場合のみsetItemを呼ぶ
      for (i in 0 until (rows * 9)) {
        val oldItem = currentSlots[i]?.item
        val newItem = newSlots[i]?.item
        
        if (oldItem != newItem) {
          inventory.setItem(i, newItem)
        }
      }
    }

    // Save current slots for event handling.
    this.currentSlots = newSlots.toMap()

    // Update the inventory for all viewers.
    viewers.forEach { it.updateInventory() }
  }

  internal var currentSlots: Map<Int, SlotCreator> = emptyMap()

  /**
   * Opens the GUI for the specified player.
   *
   * @param player The player to open the GUI for.
   */
  fun open(player: Player) {
    if (!::inventory.isInitialized) {
      update()
    }
    holder.addViewer(player)
    player.openInventory(inventory)
  }
}

/**
 * A context class that provides a more declarative DSL for building GUIs.
 * This class also supports state management.
 *
 * Example usage:
 * ```kotlin
 * val gui = inventory(3, Component.text("My GUI")) {
 *     var counter by state(0)
 *
 *     slot(SLOT_2_5) {
 *         icon(Material.DIAMOND)
 *         name(Component.text("Clicked $counter times"))
 *         onClick {
 *             counter++ // This automatically triggers update()
 *         }
 *     }
 * }
 * gui.open(player)
 * ```
 */
@GuiDsl
abstract class GuiContext(rows: Int) : Gui(rows) {
  /**
   * Builds the GUI. This is where you define slots and states.
   */
  abstract fun build()

  final override fun build(creator: GuiCreator) {
    this.creator = creator
    build()
  }

  private lateinit var creator: GuiCreator

  /**
   * Sets the title of the GUI.
   */
  fun title(title: net.kyori.adventure.text.Component) {
    creator.title = title
  }

  /**
   * Configures a slot by its X and Y coordinates (1-indexed).
   */
  fun slot(x: Int, y: Int, creator: SlotCreator.() -> Unit) = this.creator.slot(x, y, creator)

  /**
   * Configures a slot by a [Slot] instance.
   */
  fun slot(slot: Slot, creator: SlotCreator.() -> Unit) = this.creator.slot(slot, creator)

  /**
   * Configures a slot by its raw index (0-indexed).
   */
  fun slot(slot: Int, creator: SlotCreator.() -> Unit) = this.creator.slot(slot, creator)

  /**
   * Fills all slots in the GUI with a material.
   */
  fun fill(material: org.bukkit.Material, pickable: Boolean = false, creator: SlotCreator.() -> Unit = {}) =
    this.creator.fill(material, pickable, creator)

  /**
   * Fills the border of the GUI with a material.
   */
  fun fillBorder(material: org.bukkit.Material, pickable: Boolean = false, creator: SlotCreator.() -> Unit = {}) =
    this.creator.fillBorder(material, pickable, creator)

  /**
   * Fills a rectangular area of the GUI with a material.
   */
  fun fillRect(
    x1: Int,
    y1: Int,
    x2: Int,
    y2: Int,
    material: org.bukkit.Material,
    pickable: Boolean = false,
    creator: SlotCreator.() -> Unit = {}
  ) =
    this.creator.fillRect(x1, y1, x2, y2, material, pickable, creator)

  /**
   * Fills a rectangular area defined by two slots with a material.
   */
  fun fillRect(
    s1: Slot,
    s2: Slot,
    material: org.bukkit.Material,
    pickable: Boolean = false,
    creator: SlotCreator.() -> Unit = {}
  ) =
    this.creator.fillRect(s1, s2, material, pickable, creator)

  /**
   * Creates a state delegate for this GUI.
   * Changing the state will automatically trigger [update].
   *
   * @param initial The initial value of the state.
   */
  fun <T> state(initial: T) = GuiState(initial)

  private val states = mutableMapOf<Int, Any?>()
  private var stateCounter = 0

  /**
   * Delegate class for GUI states.
   */
  inner class GuiState<T>(private val initial: T) {
    private val id = stateCounter++

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T {
      return states.getOrPut(id) { initial } as T
    }

    operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, newValue: T) {
      states[id] = newValue
      update()
    }
  }

  internal fun resetStateCounter() {
    stateCounter = 0
  }
}

/**
 * Listener object for processing GUI events.
 */
object GuiListener : Listener {
  /**
   * Registers the GUI listener to the plugin.
   *
   * @param plugin The plugin instance.
   */
  fun register(plugin: Plugin) {
    Bukkit.getPluginManager().registerEvents(this, plugin)
  }

  /**
   * Internal event handler for clicks in GUIs.
   */
  @EventHandler(priority = EventPriority.LOWEST)
  fun onInventoryClick(event: InventoryClickEvent) {
    val holder = event.inventory.holder as? GuiHolder ?: return
    val gui = holder.gui
    val player = event.whoClicked as? Player ?: return

    // Ensure the clicked inventory is the GUI.
    if (event.clickedInventory != event.inventory) return

    val slot = event.slot
    if (slot < 0 || slot >= gui.rows * 9) return

    val slotCreator = gui.currentSlots[slot] ?: return

    if (!slotCreator.pickable) {
      event.isCancelled = true
    }

    val guiEvent = GuiClickEvent(player, slot, event)
    slotCreator.events.forEach { it(guiEvent) }
  }

  /**
   * Internal event handler for closing GUIs.
   */
  @EventHandler(priority = EventPriority.MONITOR)
  fun onInventoryClose(event: InventoryCloseEvent) {
    val holder = event.inventory.holder as? GuiHolder ?: return
    val player = event.player as? Player ?: return
    holder.removeViewer(player)
  }
}
