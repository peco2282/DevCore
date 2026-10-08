package com.peco2282.devcore.config.serializers

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import com.peco2282.devcore.config.reflection.ClassMapper
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame

class BukkitSerializersTest {

  private lateinit var server: ServerMock

  @BeforeEach
  fun setUp() {
    server = MockBukkit.mock()
    server.addSimpleWorld("example")
    BukkitSerializers.registerAll()
  }

  @AfterEach
  fun tearDown() {
    MockBukkit.unmock()
  }

  @Test
  fun `vector supports maps and configuration sections`() {
    val fromMap = BukkitSerializers.VECTOR.deserialize(
      mapOf("x" to 1, "y" to 2.5, "z" to -3)
    )
    assertEquals(Vector(1.0, 2.5, -3.0), fromMap)

    val yaml = YamlConfiguration().apply {
      set("vector.x", 4.0)
      set("vector.y", 5.0)
      set("vector.z", 6.0)
    }
    val fromSection = BukkitSerializers.VECTOR.deserialize(
      yaml.getConfigurationSection("vector")
    )
    assertEquals(Vector(4.0, 5.0, 6.0), fromSection)
  }

  @Test
  fun `location supports world names coordinates and rotation`() {
    val location = BukkitSerializers.LOCATION.deserialize(
      mapOf(
        "world" to "example",
        "x" to 10,
        "y" to 64.5,
        "z" to -20,
        "yaw" to 90,
        "pitch" to 15
      )
    )

    assertEquals("example", location.world?.name)
    assertEquals(10.0, location.x)
    assertEquals(64.5, location.y)
    assertEquals(-20.0, location.z)
    assertEquals(90f, location.yaw)
    assertEquals(15f, location.pitch)
  }

  @Test
  fun `serializers clone already deserialized mutable values`() {
    val vector = Vector(1.0, 2.0, 3.0)
    val location = Location(server.getWorld("example"), 1.0, 2.0, 3.0)
    val item = ItemStack(Material.DIAMOND, 2)

    assertNotSame(vector, BukkitSerializers.VECTOR.deserialize(vector))
    assertNotSame(location, BukkitSerializers.LOCATION.deserialize(location))
    assertNotSame(item, BukkitSerializers.ITEM_STACK.deserialize(item))
  }

  data class BukkitConfig(
    val spawn: Location,
    val direction: Vector,
    val waypoints: List<Vector>,
    val item: ItemStack
  )

  @Test
  fun `class mapper round trips bukkit types`() {
    val yaml = YamlConfiguration().apply {
      set("spawn.world", "example")
      set("spawn.x", 1.5)
      set("spawn.y", 70.0)
      set("spawn.z", -4.5)
      set("direction", mapOf("x" to 1.0, "y" to 0.0, "z" to -1.0))
      set(
        "waypoints",
        listOf(
          mapOf("x" to 1.0, "y" to 2.0, "z" to 3.0),
          mapOf("x" to 4.0, "y" to 5.0, "z" to 6.0)
        )
      )
      set("item", ItemStack(Material.DIAMOND, 3).serialize())
    }

    val config = ClassMapper.create(BukkitConfig::class, yaml)

    assertEquals("example", config.spawn.world?.name)
    assertEquals(Vector(1.0, 0.0, -1.0), config.direction)
    assertEquals(Vector(4.0, 5.0, 6.0), config.waypoints.last())
    assertEquals(Material.DIAMOND, config.item.type)
    assertEquals(3, config.item.amount)

    val reloaded = ClassMapper.create(BukkitConfig::class, yaml)
    assertEquals(config.spawn, reloaded.spawn)
    assertEquals(config.direction, reloaded.direction)
    assertEquals(config.waypoints, reloaded.waypoints)
    assertEquals(config.item, reloaded.item)
  }

  @Test
  fun `malformed values fail with a clear error`() {
    assertFailsWith<IllegalArgumentException> {
      BukkitSerializers.VECTOR.deserialize(mapOf("x" to 1.0, "y" to 2.0))
    }
    assertFailsWith<IllegalArgumentException> {
      BukkitSerializers.LOCATION.deserialize(
        mapOf("world" to "missing", "x" to 1.0, "y" to 2.0, "z" to 3.0)
      )
    }
  }
}
