package com.peco2282.devcore.tasksequence

import org.bukkit.plugin.Plugin
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import com.peco2282.devcore.scheduler.ticks

class TaskSequenceTest {

  @Test
  fun testWaitDuration() = runBlocking {
    val plugin = dummyPlugin()
    val builder = TaskSequenceBuilder(plugin)
    // Actually we cannot test time passage easily with dummy plugin,
    // but we can test if it doesn't crash.
    builder.wait(Duration.ofMillis(50))
  }

  @Test
  fun testWaitUntil() = runBlocking {
    var count = 0
    val plugin = dummyPlugin()
    val builder = TaskSequenceBuilder(plugin)

    builder.waitUntil(checkInterval = 1.ticks) {
      count++
      count == 3
    }

    assertEquals(3, count)
  }

  @Test
  fun testRepeatEvery() = runBlocking {
    val counter = AtomicInteger(0)
    val plugin = dummyPlugin()
    val builder = TaskSequenceBuilder(plugin)

    builder.repeatEvery(3, 1.ticks) { i ->
      counter.incrementAndGet()
      assertEquals(i, counter.get() - 1)
    }

    assertEquals(3, counter.get())
  }

  @Test
  fun testTimeout() = runBlocking {
    val plugin = dummyPlugin()
    val builder = TaskSequenceBuilder(plugin)

    val result = builder.timeout(10.ticks) {
      "success"
    }
    assertEquals("success", result)
  }

  @Test
  fun testRepeat() = runBlocking {
    val counter = AtomicInteger(0)
    val plugin = dummyPlugin()
    val builder = TaskSequenceBuilder(plugin)

    builder.repeat(5) { i ->
      counter.incrementAndGet()
      assertEquals(i, counter.get() - 1)
    }

    assertEquals(5, counter.get())
  }

  @Test
  fun testRepeatWhile() = runBlocking {
    var count = 0
    val plugin = dummyPlugin()
    val builder = TaskSequenceBuilder(plugin)

    builder.repeatWhile({ count < 3 }) {
      count++
    }

    assertEquals(3, count)
  }

  @Test
  fun testRunIf() = runBlocking {
    var ran = false
    val plugin = dummyPlugin()
    val builder = TaskSequenceBuilder(plugin)

    builder.runIf({ true }) {
      ran = true
    }
    assertEquals(true, ran)

    ran = false
    builder.runIf({ false }) {
      ran = true
    }
    assertEquals(false, ran)
  }

  private fun dummyPlugin(): Plugin {
    return Proxy.newProxyInstance(
      Plugin::class.java.classLoader,
      arrayOf(Plugin::class.java)
    ) { _, method, _ ->
      when (method.name) {
        "getName" -> "TestPlugin"
        "isEnabled" -> true
        else -> null
      }
    } as Plugin
  }
}
