package com.peco2282.devcore.world

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import org.bukkit.Difficulty
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorldDslMockBukkitTest {
  private lateinit var server: ServerMock

  @BeforeEach
  fun setUp() {
    server = MockBukkit.mock()
  }

  @AfterEach
  fun tearDown() {
    MockBukkit.unmock()
  }

  @Test
  fun `world editor updates time weather difficulty and game rules`() {
    val world = server.addSimpleWorld("world")

    world.edit {
      time = 6_000L
      weather = WeatherType.THUNDER
      difficulty = Difficulty.HARD
      gameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
    }

    assertEquals(6_000L, world.time)
    assertTrue(world.hasStorm())
    assertTrue(world.isThundering)
    assertEquals(Difficulty.HARD, world.difficulty)
    assertFalse(world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE)!!)
  }

  @Test
  fun `weather convenience extension maps every weather state`() {
    val world = server.addSimpleWorld("weather")

    world.weather(WeatherType.RAIN)
    assertTrue(world.hasStorm())
    assertFalse(world.isThundering)

    world.weather(WeatherType.CLEAR)
    assertFalse(world.hasStorm())
    assertFalse(world.isThundering)
  }

  @Test
  fun `player editor updates player properties and teleports`() {
    val world = server.addSimpleWorld("players")
    val player = server.addPlayer()
    val destination = Location(world, 12.0, 70.0, -4.0)

    player.edit {
      gameMode = GameMode.CREATIVE
      health = 14.0
      foodLevel = 9
      exp = 0.5f
      level = 7
      teleport(destination)
    }

    assertEquals(GameMode.CREATIVE, player.gameMode)
    assertEquals(14.0, player.health)
    assertEquals(9, player.foodLevel)
    assertEquals(0.5f, player.exp)
    assertEquals(7, player.level)
    assertEquals(destination, player.location)
  }

  @Test
  fun `block editor changes block material`() {
    val world = server.addSimpleWorld("blocks")
    val block = world.getBlockAt(3, 64, 5)

    world.edit {
      block(3, 64, 5) { type = Material.DIAMOND_BLOCK }
    }

    assertEquals(Material.DIAMOND_BLOCK, block.type)
  }
}
