package com.peco2282.devcore.cooldown.persistent

import be.seeseemelk.mockbukkit.MockBukkit
import com.peco2282.devcore.cooldown.TimeSource
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class PersistentCooldownsTest {
  private class Clock(var now: Long = 0) : TimeSource { override fun nowMillis() = now }
  private lateinit var server: be.seeseemelk.mockbukkit.ServerMock
  private lateinit var plugin: be.seeseemelk.mockbukkit.MockPlugin
  @BeforeTest fun setup() { server = MockBukkit.mock(); plugin = MockBukkit.createMockPlugin() }
  @AfterTest fun close() = MockBukkit.unmock()

  @Test fun `deadline survives manager recreation and expires lazily`() {
    val player = server.addPlayer(); val clock = Clock()
    assertTrue(PersistentCooldowns(plugin, clock).tryUse(player, "daily", 10.seconds))
    assertFalse(PersistentCooldowns(plugin, clock).tryUse(player, "daily", 10.seconds))
    clock.now = 10_000
    assertTrue(PersistentCooldowns(plugin, clock).isReady(player, "daily"))
  }
}
