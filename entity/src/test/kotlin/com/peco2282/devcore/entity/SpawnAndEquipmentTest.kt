package com.peco2282.devcore.entity

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Zombie
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpawnAndEquipmentTest {
  private lateinit var server: ServerMock
  private lateinit var origin: Location

  @BeforeEach
  fun setUp() {
    server = MockBukkit.mock()
    origin = Location(server.addSimpleWorld("w"), 0.0, 64.0, 0.0)
  }

  @AfterEach
  fun tearDown() {
    MockBukkit.unmock()
  }

  @Test
  fun `typed spawn applies the editor to the spawned entity`() {
    val zombie = origin.spawn<Zombie> { customName(net.kyori.adventure.text.Component.text("Bob")) }
    assertEquals(EntityType.ZOMBIE, zombie.type)
    assertTrue(zombie.isValid)
    assertEquals("Bob", net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(zombie.customName()!!))
  }

  @Test
  fun `spawn by EntityType runs the editor`() {
    var edited = false
    val entity = origin.spawn(EntityType.COW) { edited = true }
    assertEquals(EntityType.COW, entity.type)
    assertTrue(edited)
  }

  @Test
  fun `equipment DSL sets armor and hand items`() {
    val zombie = origin.spawn<Zombie>()
    zombie.equipment {
      helmet = ItemStack(Material.DIAMOND_HELMET)
      itemInMainHand = ItemStack(Material.IRON_SWORD)
    }
    assertEquals(Material.DIAMOND_HELMET, zombie.equipment!!.helmet!!.type)
    assertEquals(Material.IRON_SWORD, zombie.equipment!!.itemInMainHand.type)
  }

  @Test
  fun `getNearbyEntitiesByType filters by type and radius`() {
    origin.spawn<Zombie>()
    origin.spawn(EntityType.COW)
    origin.clone().add(50.0, 0.0, 0.0).spawn<Zombie>()

    val near = origin.getNearbyEntitiesByType<Zombie>(radius = 5.0) { true }
    assertEquals(1, near.size)
  }
}
