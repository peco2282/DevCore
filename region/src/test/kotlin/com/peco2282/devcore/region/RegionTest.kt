package com.peco2282.devcore.region

import be.seeseemelk.mockbukkit.MockBukkit
import kotlin.math.PI
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.bukkit.Location
import org.bukkit.World
import be.seeseemelk.mockbukkit.ServerMock

class RegionTest {
  private lateinit var world: World
  private lateinit var server: ServerMock

  @BeforeTest fun setUp() { server = MockBukkit.mock(); world = server.addSimpleWorld("world") }
  @AfterTest fun tearDown() { MockBukkit.unmock() }

  @Test fun `cuboid normalizes bounds and includes edges`() {
    val value = cuboid(world, Location(world, 10.0, 70.0, 10.0), Location(world, 0.0, 60.0, 0.0))
    assertTrue(Location(world, 0.0, 60.0, 0.0) in value)
    assertTrue(Location(world, 10.0, 70.0, 10.0) in value)
    assertFalse(Location(world, 10.01, 70.0, 10.0) in value)
    assertEquals(Location(world, 5.0, 65.0, 5.0), value.center)
    assertEquals(1_331.0, value.volume)
    assertEquals(1_331, value.blocks().count())
  }

  @Test fun `sphere and cylinder enforce world and geometry`() {
    val sphere = sphere(Location(world, 1.0, 2.0, 3.0), 2.0)
    assertTrue(Location(world, 3.0, 2.0, 3.0) in sphere)
    assertFalse(Location(world, 3.01, 2.0, 3.0) in sphere)
    assertEquals(4.0 / 3.0 * PI * 8.0, sphere.volume, 1e-9)

    val cylinder = cylinder(Location(world, 0.0, 5.0, 0.0), 2.0, 4.0)
    assertTrue(Location(world, 2.0, 9.0, 0.0) in cylinder)
    assertFalse(Location(world, 0.0, 9.01, 0.0) in cylinder)
    assertEquals(PI * 4.0 * 4.0, cylinder.volume, 1e-9)
  }

  @Test fun `random locations always remain inside`() {
    val values = listOf(
      cuboid(world, Location(world, -4.0, 0.0, -4.0), Location(world, 4.0, 8.0, 4.0)),
      sphere(Location(world, 0.0, 4.0, 0.0), 4.0),
      cylinder(Location(world, 0.0, 0.0, 0.0), 4.0, 8.0),
    )
    values.forEach { region -> repeat(200) { assertTrue(region.randomLocation(kotlin.random.Random(it)) in region) } }
    val point = cuboid(world, Location(world, 1.0, 2.0, 3.0), Location(world, 1.0, 2.0, 3.0))
    assertEquals(Location(world, 1.0, 2.0, 3.0), point.randomLocation())
  }

  @Test fun `chunk index returns same regions as containment`() {
    val registry = RegionRegistry()
    val regions = listOf(
      cuboid(world, Location(world, -20.0, 0.0, -20.0), Location(world, 20.0, 10.0, 20.0)),
      sphere(Location(world, 40.0, 5.0, 40.0), 8.0),
      cylinder(Location(world, -40.0, 0.0, 40.0), 5.0, 10.0),
    )
    regions.forEachIndexed { index, region -> registry.add("r$index", region) }
    for (x in -60..60 step 3) for (z in -60..60 step 3) {
      val location = Location(world, x.toDouble(), 5.0, z.toDouble())
      assertEquals(regions.filter { location in it }.toSet(), registry.at(location).toSet())
    }
    assertEquals(regions[1], registry.remove("r1"))
    assertTrue(registry.at(Location(world, 40.0, 5.0, 40.0)).isEmpty())
  }

  @Test fun `membership callbacks fire only on transitions`() {
    val registry = RegionRegistry()
    registry.add("spawn", cuboid(world, Location(world, 0.0, 0.0, 0.0), Location(world, 5.0, 10.0, 5.0)))
    val player = server.addPlayer()
    var enters = 0
    var leaves = 0
    registry.onEnter("spawn") { enters++ }
    registry.onLeave("spawn") { leaves++ }

    registry.update(player, Location(world, -1.0, 1.0, -1.0))
    registry.update(player, Location(world, 1.0, 1.0, 1.0))
    registry.update(player, Location(world, 2.0, 1.0, 2.0))
    registry.update(player, Location(world, 6.0, 1.0, 6.0))

    assertEquals(1, enters)
    assertEquals(1, leaves)
  }
}
