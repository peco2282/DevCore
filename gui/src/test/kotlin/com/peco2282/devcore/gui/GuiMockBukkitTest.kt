package com.peco2282.devcore.gui

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import be.seeseemelk.mockbukkit.inventory.InventoryMock
import be.seeseemelk.mockbukkit.inventory.SimpleInventoryViewMock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GuiMockBukkitTest {
  private lateinit var server: ServerMock

  @BeforeEach
  fun setUp() {
    val titles = java.util.IdentityHashMap<Inventory, String>()
    server = MockBukkit.mock(object : ServerMock() {
      override fun createInventory(owner: InventoryHolder?, size: Int, title: Component): InventoryMock {
        val legacyTitle = LegacyComponentSerializer.legacySection().serialize(title)
        return super.createInventory(owner, size, legacyTitle).also { titles[it] = legacyTitle }
      }
    })
    val plugin = MockBukkit.createMockPlugin()
    // MockBukkit 1.20 does not propagate inventory titles into opened views.
    server.pluginManager.registerEvents(object : Listener {
      @EventHandler
      fun onOpen(event: InventoryOpenEvent) {
        titles[event.inventory]?.let { event.view.title = it }
      }
    }, plugin)
    GuiListener.register(plugin)
  }

  @AfterEach
  fun tearDown() {
    MockBukkit.unmock()
  }

  @Test
  fun `slot creator builds an item stack through Bukkit item factory`() {
    val slot = SlotCreator()
      .icon(Material.DIAMOND, 2)
      .keep()

    assertEquals(Material.DIAMOND, slot.item.type)
    assertEquals(2, slot.item.amount)
    assertFalse(slot.pickable)
  }

  @Test
  fun `click listener cancels kept slot and invokes its handler`() {
    var clicked = false
    val fixture = guiFixture(
      SlotCreator()
        .icon(Material.STONE)
        .keep()
        .onClick { clicked = true }
    )
    val event = fixture.clickEvent()

    server.pluginManager.callEvent(event)

    assertTrue(event.isCancelled)
    assertTrue(clicked)
  }

  @Test
  fun `click listener leaves a pickable slot uncancelled`() {
    val fixture = guiFixture(SlotCreator().icon(Material.STONE))
    val event = fixture.clickEvent()

    server.pluginManager.callEvent(event)

    assertFalse(event.isCancelled)
  }

  @Test
  fun `closing inventory removes player from gui viewers`() {
    val fixture = guiFixture(SlotCreator())
    fixture.gui.holder.addViewer(fixture.player)
    assertTrue(fixture.player in fixture.gui.holder.getViewers())

    server.pluginManager.callEvent(InventoryCloseEvent(fixture.view))

    assertFalse(fixture.player in fixture.gui.holder.getViewers())
  }

  private fun guiFixture(slot: SlotCreator): Fixture {
    val gui = object : Gui(1) {
      override fun build(creator: GuiCreator) = Unit
    }
    gui.currentSlots = mapOf(0 to slot)

    val inventory = InventoryMock(gui.holder, 9, InventoryType.CHEST)
    gui.holder.setInventory(inventory)
    inventory.setItem(0, slot.item)

    val player = server.addPlayer()
    val view = SimpleInventoryViewMock(
      player,
      inventory,
      player.inventory,
      InventoryType.CHEST
    )
    return Fixture(gui, player, view)
  }

  @Test
  fun `rectangle includes the final row with reversed endpoints`() {
    val creator = GuiCreator(3)

    creator.fillRect(3, 9, 2, 8, Material.STONE)

    assertEquals(setOf(16, 17, 25, 26), creator.getSlots().keys)
  }

  @Test
  fun `rectangle fills a single cell in a one row gui`() {
    val creator = GuiCreator(1)

    creator.fillRect(1, 9, 1, 9, Material.DIAMOND)

    assertEquals(setOf(8), creator.getSlots().keys)
    assertEquals(Material.DIAMOND, creator.getSlots().getValue(8).item.type)
  }

  @Test
  fun `rectangle clips coordinates outside the inventory`() {
    val creator = GuiCreator(2)

    creator.fillRect(-1, -1, 3, 11, Material.STONE)

    assertEquals((0 until 18).toSet(), creator.getSlots().keys)
  }

  @Test
  fun `rectangle entirely outside the inventory adds no slots`() {
    val creator = GuiCreator(2)

    creator.fillRect(-2, 1, 0, 9, Material.STONE)
    creator.fillRect(3, 1, 4, 9, Material.STONE)
    creator.fillRect(1, 10, 2, 12, Material.STONE)

    assertTrue(creator.getSlots().isEmpty())
  }

  @Test
  fun `successive title changes preserve all viewers and normal close removes them`() {
    var title = Component.text("initial")
    val gui = object : Gui(1) {
      override fun build(creator: GuiCreator) {
        creator.title = title
        creator.slot(0) { icon(Material.DIAMOND) }
      }
    }
    val players = listOf(server.addPlayer(), server.addPlayer())
    players.forEach { gui.open(it) }

    repeat(3) { index ->
      val previousViews = players.map { it.openInventory }
      title = Component.text("title $index")
      gui.update()
      // Deliver the old inventory close event explicitly as well, so stale closes are covered.
      previousViews.forEach { server.pluginManager.callEvent(InventoryCloseEvent(it)) }

      assertEquals(players.toSet(), gui.holder.getViewers())
      players.forEach {
        assertEquals(gui.inventory, it.openInventory.topInventory)
        assertEquals(title, it.openInventory.title())
        assertEquals(Material.DIAMOND, it.openInventory.topInventory.getItem(0)?.type)
      }
    }

    players.forEach { it.closeInventory() }
    assertTrue(gui.holder.getViewers().isEmpty())
  }

  private data class Fixture(
    val gui: Gui,
    val player: org.bukkit.entity.Player,
    val view: SimpleInventoryViewMock
  ) {
    fun clickEvent() = InventoryClickEvent(
      view,
      InventoryType.SlotType.CONTAINER,
      0,
      ClickType.LEFT,
      InventoryAction.PICKUP_ALL
    )
  }
}
