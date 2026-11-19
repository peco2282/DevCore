package com.peco2282.devcore.kit
import be.seeseemelk.mockbukkit.MockBukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.test.*

class KitTest {
  private lateinit var server: be.seeseemelk.mockbukkit.ServerMock
  @BeforeTest fun setup() { server = MockBukkit.mock() }
  @AfterTest fun close() = MockBukkit.unmock()
  @Test fun `captured items are cloned and empty slots ignored`() {
    val plugin = MockBukkit.createMockPlugin(); val player = server.addPlayer()
    player.inventory.setItem(0, ItemStack(Material.STONE, 3))
    val kit = plugin.kits().saveFrom(player.inventory, "saved")
    player.inventory.getItem(0)!!.amount = 1
    assertEquals(3, kit.items.single().amount)
  }
}
