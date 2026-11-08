package com.peco2282.devcore.effect

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import com.peco2282.devcore.scheduler.seconds
import com.peco2282.devcore.scheduler.ticks
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlaybackTest {
  private companion object { var next = 0 }

  private lateinit var server: ServerMock
  private lateinit var plugin: Plugin
  private lateinit var origin: Location
  private val frames = mutableListOf<Int>()
  private val counting = Effect { _, _, frame -> frames += frame }

  private fun tick(n: Int) = repeat(n) { server.scheduler.performOneTick() }

  @BeforeEach
  fun setUp() {
    server = MockBukkit.mock()
    plugin = MockBukkit.createMockPlugin("fx-${next++}")
    origin = Location(server.addSimpleWorld("w"), 0.0, 64.0, 0.0)
  }

  @AfterEach
  fun tearDown() = MockBukkit.unmock()

  @Test
  fun `plays immediately then every period with increasing frames`() {
    plugin.playEffect(counting, origin, every = 5.ticks)
    tick(1)
    assertEquals(listOf(0), frames)
    tick(10)
    assertEquals(listOf(0, 1, 2), frames)
  }

  @Test
  fun `stops after the duration`() {
    val handle = plugin.playEffect(counting, origin, every = 10.ticks, duration = 1.seconds)
    tick(200)
    assertEquals(2, frames.size)
    assertTrue(handle.isCancelled)
  }

  @Test
  fun `cancel stops playback`() {
    val handle = plugin.playEffect(counting, origin, every = 2.ticks)
    tick(5)
    handle.cancel()
    val seen = frames.size
    tick(20)
    assertEquals(seen, frames.size)
  }

  @Test
  fun `following an entity stops when it is removed`() {
    val cow = origin.world.spawn(origin, org.bukkit.entity.Cow::class.java)
    plugin.playEffect(counting, cow, every = 2.ticks)
    tick(5)
    assertTrue(frames.isNotEmpty())
    cow.remove()
    tick(2)
    val seen = frames.size
    tick(20)
    assertEquals(seen, frames.size)
  }

  @Test
  fun `location supplier returning null stops playback`() {
    var alive = true
    val handle = plugin.playEffect(counting, every = 2.ticks) { if (alive) origin else null }
    tick(3)
    alive = false
    tick(4)
    assertTrue(handle.isCancelled)
  }

  @Test
  fun `animated effect receives the frame number`() {
    val seenFrames = mutableListOf<Int>()
    val anim = animatedEffect { frame -> seenFrames += frame }
    anim.play(origin, null, 7)
    assertEquals(listOf(7), seenFrames)
  }

  @Test
  fun `built effect with no steps is a harmless no-op`() {
    effect { }.play(origin)
    assertFalse(frames.isNotEmpty())
  }
}
