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
 * 内部用のインベントリホルダー
 */
internal class GuiHolder(val gui: Gui) : InventoryHolder {
  private var inventory: Inventory? = null
  private val viewers = mutableSetOf<Player>()

  fun addViewer(player: Player) {
    viewers.add(player)
  }

  fun removeViewer(player: Player) {
    viewers.remove(player)
  }

  fun getViewers(): Set<Player> = viewers

  fun setInventory(inventory: Inventory) {
    this.inventory = inventory
  }

  override fun getInventory(): Inventory {
    return inventory ?: throw IllegalStateException("Inventory not initialized")
  }
}

/**
 * GUIの基本クラス
 */
abstract class Gui(val rows: Int) {
  internal val holder = GuiHolder(this)
  lateinit var inventory: Inventory
    private set

  init {
    require(rows in 1..6) { "rows must be between 1 and 6" }
  }

  /**
   * GUIを構築します。
   */
  abstract fun build(creator: GuiCreator)

  /**
   * インベントリを生成または更新します。
   */
  fun update() {
    if (this is GuiContext) {
      resetStateCounter()
    }
    val creator = GuiCreator(rows)
    build(creator)

    if (!::inventory.isInitialized) {
      inventory = Bukkit.createInventory(holder, rows * 9, creator.title)
      holder.setInventory(inventory)
    } else {
      // タイトルの更新が必要な場合は、新しいインベントリを作成して開かせ直す必要がある（Bukkitの制約）
      // ここでは簡易的にアイテムのみの更新とする
    }

    // スロットをクリアしてから再配置
    inventory.clear()
    creator.getSlots().forEach { (slot, slotCreator) ->
      inventory.setItem(slot, slotCreator.item)
    }

    // 現在のスロット設定を保存（イベント処理用）
    this.currentSlots = creator.getSlots().toMap()

    // プレイヤーに開かれているインベントリの表示を更新
    holder.getViewers().forEach { it.updateInventory() }
  }

  internal var currentSlots: Map<Int, SlotCreator> = emptyMap()

  fun open(player: Player) {
    if (!::inventory.isInitialized) {
      update()
    }
    holder.addViewer(player)
    player.openInventory(inventory)
  }
}

/**
 * 構築中にGUI自体へのアクセスを提供するためのマーカー
 */
@GuiDsl
abstract class GuiContext(rows: Int) : Gui(rows) {
  /**
   * GUIを構築します。
   */
  abstract fun build()

  final override fun build(creator: GuiCreator) {
    this.creator = creator
    build()
  }

  private lateinit var creator: GuiCreator

  fun title(title: net.kyori.adventure.text.Component) {
    creator.title = title
  }

  fun slot(x: Int, y: Int, creator: SlotCreator.() -> Unit) = this.creator.slot(x, y, creator)

  fun slot(slot: Slot, creator: SlotCreator.() -> Unit) = this.creator.slot(slot, creator)

  fun slot(slot: Int, creator: SlotCreator.() -> Unit) = this.creator.slot(slot, creator)

  fun fill(material: org.bukkit.Material, pickable: Boolean = false, creator: SlotCreator.() -> Unit = {}) =
    this.creator.fill(material, pickable, creator)

  fun fillBorder(material: org.bukkit.Material, pickable: Boolean = false, creator: SlotCreator.() -> Unit = {}) =
    this.creator.fillBorder(material, pickable, creator)

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

  fun fillRect(
    s1: Slot,
    s2: Slot,
    material: org.bukkit.Material,
    pickable: Boolean = false,
    creator: SlotCreator.() -> Unit = {}
  ) =
    this.creator.fillRect(s1, s2, material, pickable, creator)

  /**
   * 状態を保持するためのデリゲート。
   */
  fun <T> state(initial: T) = GuiState(initial)

  private val states = mutableMapOf<Int, Any?>()
  private var stateCounter = 0

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
 * GUIイベントを処理するリスナー
 */
object GuiListener : Listener {
  fun register(plugin: Plugin) {
    Bukkit.getPluginManager().registerEvents(this, plugin)
  }

  @EventHandler(priority = EventPriority.LOWEST)
  fun onInventoryClick(event: InventoryClickEvent) {
    val holder = event.inventory.holder as? GuiHolder ?: return
    val gui = holder.gui
    val player = event.whoClicked as? Player ?: return

    // クリックされたインベントリがGUIのインベントリであることを確認
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

  @EventHandler(priority = EventPriority.MONITOR)
  fun onInventoryClose(event: InventoryCloseEvent) {
    val holder = event.inventory.holder as? GuiHolder ?: return
    val player = event.player as? Player ?: return
    holder.removeViewer(player)
  }
}
