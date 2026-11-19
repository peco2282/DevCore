package com.peco2282.devcore.teleport

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World

class TeleportTest {
  private lateinit var server: ServerMock
  private lateinit var world: World

  @BeforeTest
  fun setUp() {
    server = MockBukkit.mock()
    world = server.addSimpleWorld("world")
  }

  @AfterTest
  fun tearDown() = MockBukkit.unmock()

  @Test
  fun `safe destination requires floor and two clear body blocks`() {
    world.getBlockAt(0, 63, 0).type = Material.STONE
    val destination = Location(world, 0.5, 64.0, 0.5)
    assertTrue(destination.isSafeTeleportDestination())

    world.getBlockAt(0, 64, 0).type = Material.LAVA
    assertFalse(destination.isSafeTeleportDestination())
    world.getBlockAt(0, 64, 0).type = Material.AIR
    world.getBlockAt(0, 63, 0).type = Material.MAGMA_BLOCK
    assertFalse(destination.isSafeTeleportDestination())
  }

  @Test
  fun `findSafe searches vertically and returns centered feet location`() {
    world.getBlockAt(0, 63, 0).type = Material.STONE
    val found = Location(world, 0.1, 66.0, 0.9, 45f, 10f).findSafe(radius = 0, verticalRadius = 3)
    assertNotNull(found)
    assertEquals(0.5, found.x)
    assertEquals(64.0, found.y)
    assertEquals(0.5, found.z)
    assertEquals(45f, found.yaw)

    world.getBlockAt(0, 63, 0).type = Material.AIR
    assertNull(Location(world, 0.0, 66.0, 0.0).findSafe(radius = 0, verticalRadius = 2))
  }

  @Test
  fun `warmup completes after configured ticks`() {
    val state = WarmupState(totalTicks = 3, movementTolerance = 0.5)
    assertEquals(WarmupStatus.ACTIVE, state.status)
    assertEquals(2, state.tick())
    assertEquals(1, state.tick())
    assertEquals(0, state.tick())
    assertEquals(WarmupStatus.COMPLETED, state.status)
    assertNull(state.cancelReason)
  }

  @Test
  fun `warmup cancellation is terminal and preserves first reason`() {
    val state = WarmupState(totalTicks = 20, movementTolerance = 0.5)
    assertFalse(state.moved(0.24))
    assertTrue(state.moved(0.26))
    assertEquals(WarmupStatus.CANCELLED, state.status)
    assertEquals(CancelReason.MOVED, state.cancelReason)
    assertFalse(state.cancel(CancelReason.DAMAGED))
    assertEquals(CancelReason.MOVED, state.cancelReason)
  }

  @Test
  fun `invalid warmup inputs are rejected`() {
    kotlin.test.assertFailsWith<IllegalArgumentException> { WarmupState(0, 0.5) }
    kotlin.test.assertFailsWith<IllegalArgumentException> { WarmupState(1, -0.1) }
  }
}
