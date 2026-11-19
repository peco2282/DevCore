package com.peco2282.devcore.event

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PingEvent(val value: Int) : Event(), Cancellable {
  private var cancelled = false
  override fun isCancelled() = cancelled
  override fun setCancelled(cancel: Boolean) { cancelled = cancel }
  override fun getHandlers(): HandlerList = handlerList

  companion object {
    @JvmStatic
    val handlerList = HandlerList()
  }
}

class EventDslTest {
  private lateinit var server: ServerMock
  private lateinit var plugin: Plugin

  @BeforeEach
  fun setUp() {
    server = MockBukkit.mock()
    plugin = MockBukkit.createMockPlugin()
  }

  @AfterEach
  fun tearDown() {
    MockBukkit.unmock()
  }

  private fun fire(value: Int) = PingEvent(value).also { server.pluginManager.callEvent(it) }

  @Test
  fun `handle receives events`() {
    val seen = mutableListOf<Int>()
    plugin.on<PingEvent> { handle { seen += value } }
    fire(1); fire(2)
    assertEquals(listOf(1, 2), seen)
  }

  @Test
  fun `filter limits handled events and filters combine with and`() {
    val seen = mutableListOf<Int>()
    plugin.on<PingEvent> {
      filter { value > 1 }
      filter { value < 4 }
      handle { seen += value }
    }
    (1..5).forEach { fire(it) }
    assertEquals(listOf(2, 3), seen)
  }

  @Test
  fun `once handles a single event then unregisters`() {
    var count = 0
    plugin.listenOnce<PingEvent> { count++ }
    fire(1); fire(2)
    assertEquals(1, count)
  }

  @Test
  fun `take handles exactly N events`() {
    var count = 0
    plugin.on<PingEvent> { take(3); handle { count++ } }
    repeat(10) { fire(it) }
    assertEquals(3, count)
  }

  @Test
  fun `takeWhile stops after predicate first fails`() {
    val seen = mutableListOf<Int>()
    plugin.on<PingEvent> { takeWhile { value < 3 }; handle { seen += value } }
    listOf(1, 2, 3, 1).forEach { fire(it) }
    assertEquals(listOf(1, 2), seen)
  }

  @Test
  fun `unregister stops delivery and is idempotent`() {
    var count = 0
    val builder = plugin.on<PingEvent> { handle { count++ } }
    fire(1)
    builder.unregister()
    builder.unregister()
    fire(2)
    assertEquals(1, count)
  }

  @Test
  fun `cancel marks cancellable events`() {
    plugin.on<PingEvent> { handle { cancel() } }
    assertTrue(fire(1).isCancelled)
  }

  @Test
  fun `group unregisterAll removes every handler`() {
    var count = 0
    val group = plugin.events {
      on<PingEvent> { handle { count++ } }
      on<PingEvent> { handle { count++ } }
    }
    fire(1)
    assertEquals(2, count)
    group.unregisterAll()
    fire(2)
    assertEquals(2, count)
  }

  @Test
  fun `expireAfter unregisters after the delay`() {
    var count = 0
    plugin.on<PingEvent> { expireAfter(1); handle { count++ } }
    fire(1)
    server.scheduler.performTicks(25)
    fire(2)
    assertEquals(1, count)
  }
}
