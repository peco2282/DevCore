package com.peco2282.devcore.scheduler

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import com.peco2282.devcore.scheduler.coroutines.coroutineExceptionHandler
import com.peco2282.devcore.scheduler.coroutines.dispatcher
import com.peco2282.devcore.scheduler.coroutines.scope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SchedulerLifecycleMockBukkitTest {
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
  fun `plugin scope is shared per plugin and isolated between plugins`() {
    val first = MockBukkit.createMockPlugin("first")
    val second = MockBukkit.createMockPlugin("second")

    assertSame(first.scope, first.scope)
    assertNotSame(first.scope, second.scope)

    server.pluginManager.callEvent(PluginDisableEvent(first))
    server.pluginManager.callEvent(PluginDisableEvent(second))
  }

  @Test
  fun `plugin disable cancels scheduler coroutines`() {
    val plugin: Plugin = MockBukkit.createMockPlugin("scheduler-coroutine-test")
    val handle = plugin.scheduler.launch { awaitCancellation() }

    server.pluginManager.callEvent(PluginDisableEvent(plugin))

    assertTrue(handle.isCancelled)
  }

  @Test
  fun `plugin scope cancellation runs finally blocks on disable`() {
    val plugin: Plugin = MockBukkit.createMockPlugin("scope-finally-test")
    val finalized = CompletableDeferred<Unit>()
    val job = plugin.scope.launch(start = CoroutineStart.UNDISPATCHED) {
      try {
        awaitCancellation()
      } finally {
        finalized.complete(Unit)
      }
    }

    server.pluginManager.callEvent(PluginDisableEvent(plugin))

    assertTrue(job.isCancelled)
    assertTrue(finalized.isCompleted)
  }

  @Test
  fun `plugin disable cancels scheduled bukkit tasks`() {
    val plugin: Plugin = MockBukkit.createMockPlugin("bukkit-task-test")
    val handle = plugin.scheduler.later(10.ticks) {}

    server.pluginManager.callEvent(PluginDisableEvent(plugin))

    assertTrue(handle.isCancelled)
  }

  @Test
  fun `plugin disable releases coroutine extension caches`() {
    val plugin: Plugin = MockBukkit.createMockPlugin("coroutine-cache-test")
    val scope = plugin.scope
    val dispatcher = plugin.dispatcher
    val exceptionHandler = plugin.coroutineExceptionHandler

    server.pluginManager.callEvent(PluginDisableEvent(plugin))

    assertNotSame(scope, plugin.scope)
    assertNotSame(dispatcher, plugin.dispatcher)
    assertNotSame(exceptionHandler, plugin.coroutineExceptionHandler)

    server.pluginManager.callEvent(PluginDisableEvent(plugin))
  }
}