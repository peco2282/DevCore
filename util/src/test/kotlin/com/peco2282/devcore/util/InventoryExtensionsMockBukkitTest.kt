package com.peco2282.devcore.util

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.inventory.InventoryMock
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InventoryExtensionsMockBukkitTest {
  @BeforeEach
  fun setUp() {
    MockBukkit.mock()
  }

  @AfterEach
  fun tearDown() {
    MockBukkit.unmock()
  }

  @Test
  fun `counts matching material across multiple stacks`() {
    val inventory = inventoryWith(
      ItemStack(Material.DIAMOND, 20),
      ItemStack(Material.STONE, 64),
      ItemStack(Material.DIAMOND, 7)
    )

    assertEquals(27, inventory.countItem(Material.DIAMOND))
    assertTrue(inventory.hasItem(Material.DIAMOND, 27))
    assertFalse(inventory.hasItem(Material.DIAMOND, 28))
  }

  @Test
  fun `safe removal consumes whole and partial stacks`() {
    val inventory = inventoryWith(
      ItemStack(Material.DIAMOND, 5),
      ItemStack(Material.DIAMOND, 8)
    )

    assertTrue(inventory.removeItemSafely(Material.DIAMOND, 9))

    assertNull(inventory.getItem(0))
    assertEquals(4, inventory.getItem(1)?.amount)
    assertEquals(4, inventory.countItem(Material.DIAMOND))
  }

  @Test
  fun `failed safe removal does not modify inventory`() {
    val inventory = inventoryWith(ItemStack(Material.EMERALD, 3))

    assertFalse(inventory.removeItemSafely(Material.EMERALD, 4))

    assertEquals(3, inventory.getItem(0)?.amount)
  }

  @Test
  fun `full inventory is detected and overflow items are returned`() {
    val inventory = InventoryMock(null, 9, InventoryType.CHEST)
    repeat(inventory.size) { inventory.setItem(it, ItemStack(Material.STONE, 64)) }

    assertTrue(inventory.isFull())
    val overflow = inventory.addItems(ItemStack(Material.DIAMOND, 2))

    assertEquals(1, overflow.size)
    assertEquals(Material.DIAMOND, overflow.single().type)
    assertEquals(2, overflow.single().amount)
  }

  private fun inventoryWith(vararg items: ItemStack): InventoryMock =
    InventoryMock(null, 9, InventoryType.CHEST).apply {
      items.forEachIndexed(::setItem)
    }
}
