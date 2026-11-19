package com.peco2282.devcore.storage

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import be.seeseemelk.mockbukkit.entity.PlayerMock
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.io.File
import java.net.InetAddress
import java.nio.file.Files
import java.util.UUID
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration

data class Stats(var kills: Int = 0, var coins: Long = 5, var name: String = "new", var tags: List<String> = emptyList())

class PlayerDataTest {
  private lateinit var server: ServerMock
  private lateinit var plugin: Plugin
  private lateinit var dir: File

  private companion object { var next = 0 }

  @BeforeEach
  fun setUp() {
    server = MockBukkit.mock()
    plugin = MockBukkit.createMockPlugin("storage-${next++}")
    dir = Files.createTempDirectory("playerdata").toFile()
  }

  @AfterEach
  fun tearDown() {
    MockBukkit.unmock()
    dir.deleteRecursively()
  }

  private fun data(
    onLoadFailure: LoadFailure = LoadFailure.KICK
  ) = plugin.playerData<Stats>(dir, autosave = Duration.ZERO, onLoadFailure = onLoadFailure)

  /** Fires the pre-login event the way the server does: off the main thread. */
  private fun preLogin(id: UUID, name: String = "p"): AsyncPlayerPreLoginEvent {
    val event = AsyncPlayerPreLoginEvent(name, InetAddress.getLoopbackAddress(), id)
    thread { server.pluginManager.callEvent(event) }.join()
    return event
  }

  private fun join(id: UUID, name: String = "p"): PlayerMock {
    preLogin(id, name)
    return PlayerMock(server, name, id).also { server.addPlayer(it) }
  }

  private fun file(id: UUID) = File(dir, "$id.yml")

  @Test
  fun `a new player gets default data before joining`() {
    val data = data()
    val id = UUID.randomUUID()
    assertFalse(data.isLoaded(id))
    val player = join(id)
    assertEquals(Stats(), data[player])
  }

  @Test
  fun `data is available through the player and by id, and unloaded access fails clearly`() {
    val data = data()
    val player = join(UUID.randomUUID())
    assertTrue(data[player] === data[player.uniqueId])
    val e = assertFailsWith<IllegalStateException> { data[UUID.randomUUID()] }
    assertTrue("not loaded" in e.message!!)
    assertNull(data.getOrNull(UUID.randomUUID()))
  }

  @Test
  fun `saved data is written and restored on the next login`() {
    val data = data()
    val id = UUID.randomUUID()
    val player = join(id)
    data[player].apply { kills = 7; coins = 100; name = "Bob"; tags = listOf("a", "b") }
    data.save(id).get()
    assertTrue(file(id).isFile)

    player.disconnect()
    data.saveAll().get()
    val again = join(id)
    assertEquals(Stats(7, 100, "Bob", listOf("a", "b")), data[again])
  }

  @Test
  fun `quitting saves and unloads`() {
    val data = data()
    val id = UUID.randomUUID()
    val player = join(id)
    data[player].kills = 3
    player.disconnect()
    assertFalse(data.isLoaded(id))
    // the write is asynchronous: wait for it through the next login, which waits for pending saves
    val again = join(id)
    assertEquals(3, data[again].kills)
  }

  @Test
  fun `fields missing from an older file take their defaults`() {
    val id = UUID.randomUUID()
    file(id).writeText("kills: 9\n")
    val data = data()
    val player = join(id)
    assertEquals(Stats(kills = 9), data[player])
  }

  @Test
  fun `writes leave no temporary file behind`() {
    val data = data()
    val id = UUID.randomUUID()
    join(id)
    data.save(id).get()
    assertEquals(listOf("$id.yml"), dir.list()!!.toList())
  }

  @Test
  fun `corrupt data keeps the player out by default and is set aside`() {
    val id = UUID.randomUUID()
    file(id).writeText("kills: [unclosed\n  : :")
    val data = data()
    val event = preLogin(id)
    assertEquals(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, event.loginResult)
    assertFalse(data.isLoaded(id))
    assertFalse(file(id).exists())
    assertTrue(dir.list()!!.any { it.startsWith("$id.yml.corrupt-") })
  }

  @Test
  fun `corrupt data can fall back to defaults`() {
    val id = UUID.randomUUID()
    file(id).writeText("kills: [unclosed\n  : :")
    val data = data(LoadFailure.USE_DEFAULT)
    val event = preLogin(id)
    assertEquals(AsyncPlayerPreLoginEvent.Result.ALLOWED, event.loginResult)
    assertEquals(Stats(), data[id])
    assertTrue(dir.list()!!.any { it.startsWith("$id.yml.corrupt-") })
  }

  @Test
  fun `a denied login does not load data`() {
    val data = data()
    val id = UUID.randomUUID()
    val event = AsyncPlayerPreLoginEvent("p", InetAddress.getLoopbackAddress(), id)
    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, net.kyori.adventure.text.Component.text("banned"))
    thread { server.pluginManager.callEvent(event) }.join()
    assertFalse(data.isLoaded(id))
  }

  @Test
  fun `peek reads offline players without caching and edit changes them`() {
    val data = data()
    val id = UUID.randomUUID()
    assertNull(data.peek(id).get())
    data.edit(id) { it.coins = 50; it.name = "Offline" }.get()
    assertEquals(Stats(coins = 50, name = "Offline"), data.peek(id).get())
    assertFalse(data.isLoaded(id))
  }

  @Test
  fun `edit on an online player changes the live object and saves it`() {
    val data = data()
    val id = UUID.randomUUID()
    val player = join(id)
    data.edit(id) { it.kills = 4 }.get()
    assertEquals(4, data[player].kills)
    assertTrue(file(id).readText().contains("kills: 4"))
  }

  @Test
  fun `delete removes stored data for offline players only`() {
    val data = data()
    val id = UUID.randomUUID()
    data.edit(id) { it.kills = 1 }.get()
    data.delete(id).get()
    assertFalse(file(id).exists())

    val online = join(UUID.randomUUID())
    assertTrue(data.delete(online.uniqueId).isCompletedExceptionally)
  }

  @Test
  fun `close saves everyone and stops handling events`() {
    val data = data()
    val id = UUID.randomUUID()
    val player = join(id)
    data[player].kills = 11
    data.close()
    assertTrue(file(id).readText().contains("kills: 11"))

    val other = UUID.randomUUID()
    preLogin(other)
    assertFalse(data.isLoaded(other))
    data.close() // idempotent
  }

  @Test
  fun `a custom backend and default factory are used`() {
    val store = mutableMapOf<UUID, String>()
    val backend = object : DataBackend<Stats> {
      override fun encode(value: Stats) = "${value.kills}"
      override fun decode(text: String) = Stats(kills = text.toInt())
      override fun read(id: UUID) = store[id]
      override fun write(id: UUID, text: String) { store[id] = text }
      override fun delete(id: UUID) { store.remove(id) }
    }
    val data = plugin.playerData(backend, default = { Stats(kills = -1) }, autosave = Duration.ZERO)
    val id = UUID.randomUUID()
    val player = join(id)
    assertEquals(-1, data[player].kills)
    data[player].kills = 8
    data.save(id).get()
    assertEquals("8", store[id])
    assertNotNull(data.peek(id).get())
  }

  @Test
  fun `autosave writes on the configured interval`() {
    val data = plugin.playerData<Stats>(dir, autosave = kotlin.time.Duration.parse("2s"))
    val id = UUID.randomUUID()
    val player = join(id)
    data[player].kills = 6
    assertFalse(file(id).exists())
    repeat(45) { server.scheduler.performOneTick() }
    // the write itself runs on the storage thread
    val deadline = System.currentTimeMillis() + 3000
    while (!file(id).exists() && System.currentTimeMillis() < deadline) Thread.sleep(20)
    assertTrue(file(id).readText().contains("kills: 6"))
    data.close()
  }
}
