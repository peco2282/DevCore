package com.peco2282.devcore.gui

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import be.seeseemelk.mockbukkit.inventory.InventoryMock
import be.seeseemelk.mockbukkit.inventory.SimpleInventoryViewMock
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
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
    server = MockBukkit.mock()
    GuiListener.register(MockBukkit.createMockPlugin())
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
