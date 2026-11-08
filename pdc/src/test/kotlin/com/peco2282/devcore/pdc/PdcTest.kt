package com.peco2282.devcore.pdc

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Extension-property delegates must be top level. A real plugin passes its instance: pdc(plugin, "kills", 0).
private fun key(name: String) = NamespacedKey.fromString("pdctest:$name")!!
private var Player.kills: Int by pdc(key("kills"), default = 0)
private var Player.nick: String? by pdcOrNull(key("nick"))
private var ItemStack.level: Int by itemPdc(key("level"), 1)

enum class Rank { MEMBER, VIP }
data class Color3(val r: Int, val g: Int, val b: Int)

class PdcTest {
  private lateinit var server: ServerMock
  private lateinit var plugin: Plugin
  private lateinit var player: Player

  private companion object { var next = 0 }

  @BeforeEach
  fun setUp() {
    server = MockBukkit.mock()
    plugin = MockBukkit.createMockPlugin("pdc-${next++}")
    player = server.addPlayer()
  }

  @AfterEach
  fun tearDown() = MockBukkit.unmock()

  @Test
  fun `primitives round trip on a player`() {
    val strK = pdcKey<String>(plugin, "s"); val intK = pdcKey<Int>(plugin, "i")
    val longK = pdcKey<Long>(plugin, "l"); val dblK = pdcKey<Double>(plugin, "d")
    val boolK = pdcKey<Boolean>(plugin, "b"); val byteK = pdcKey<Byte>(plugin, "y")
    player[strK] = "hi"; player[intK] = 7; player[longK] = 1L shl 40
    player[dblK] = 2.5; player[boolK] = true; player[byteK] = 3
    assertEquals("hi", player[strK]); assertEquals(7, player[intK]); assertEquals(1L shl 40, player[longK])
    assertEquals(2.5, player[dblK]); assertEquals(true, player[boolK]); assertEquals(3.toByte(), player[byteK])
  }

  @Test
  fun `absent value is null and contains reflects presence`() {
    val k = pdcKey<Int>(plugin, "x")
    assertNull(player[k])
    assertFalse(k in player)
    player[k] = 1
    assertTrue(k in player)
    player.remove(k)
    assertNull(player[k])
  }

  @Test
  fun `arrays uuid and enums are supported`() {
    val c = player.persistentDataContainer
    val arr = pdcKey<IntArray>(plugin, "arr"); val id = pdcKey<UUID>(plugin, "id"); val rank = pdcKey<Rank>(plugin, "rank")
    val uuid = UUID.randomUUID()
    c[arr] = intArrayOf(1, 2, 3); c[id] = uuid; c[rank] = Rank.VIP
    assertContentEquals(intArrayOf(1, 2, 3), c[arr]); assertEquals(uuid, c[id]); assertEquals(Rank.VIP, c[rank])
  }

  @Test
  fun `lists of primitives are supported`() {
    val c = player.persistentDataContainer
    val names = pdcKey<List<String>>(plugin, "names"); val nums = pdcKey<List<Int>>(plugin, "nums")
    c[names] = listOf("a", "b"); c[nums] = listOf(1, 2, 3)
    assertEquals(listOf("a", "b"), c[names]); assertEquals(listOf(1, 2, 3), c[nums])
  }

  @Test
  fun `custom string backed types can be registered`() {
    PdcTypes.registerString(Color3::class, { "${it.r},${it.g},${it.b}" }, { s -> s.split(",").map(String::toInt).let { Color3(it[0], it[1], it[2]) } })
    val k = pdcKey<Color3>(plugin, "color")
    player[k] = Color3(1, 2, 3)
    assertEquals(Color3(1, 2, 3), player[k])
  }

  @Test
  fun `unsupported types and bad names fail early with clear errors`() {
    class Unknown
    assertFailsWith<IllegalArgumentException> { pdcKey<Unknown>(plugin, "u") }
    assertFailsWith<IllegalArgumentException> { pdcKey<List<Unknown>>(plugin, "lu") }
    assertFailsWith<IllegalArgumentException> { pdcKey<Int>(plugin, "Has Spaces") }
  }

  @Test
  fun `getOrDefault getOrPut and update`() {
    val c = player.persistentDataContainer
    val k = pdcKey<Int>(plugin, "n")
    assertEquals(5, c.getOrDefault(k, 5)); assertNull(c[k])
    assertEquals(9, c.getOrPut(k) { 9 }); assertEquals(9, c[k])
    assertEquals(10, c.update(k, default = 0) { it + 1 }); assertEquals(10, c[k])
    val fresh = pdcKey<Int>(plugin, "fresh")
    assertEquals(1, c.update(fresh, default = 0) { it + 1 })
  }

  @Test
  fun `keys with the same name are equal`() {
    assertEquals(pdcKey<Int>(plugin, "same"), pdcKey<Int>(plugin, "same"))
  }

  @Test
  fun `item edits are written back and survive cloning`() {
    val owner = pdcKey<UUID>(plugin, "owner")
    val item = ItemStack(Material.DIAMOND_SWORD)
    assertNull(item[owner])
    item[owner] = player.uniqueId
    assertEquals(player.uniqueId, item[owner])
    assertEquals(player.uniqueId, item.clone()[owner])
    assertTrue(owner in item)
    item.removePdc(owner)
    assertFalse(owner in item)
  }

  @Test
  fun `editPdc batches several writes`() {
    val a = pdcKey<Int>(plugin, "a"); val b = pdcKey<String>(plugin, "b")
    val item = ItemStack(Material.STICK).editPdc { this[a] = 1; this[b] = "x" }
    assertEquals(1, item[a]); assertEquals("x", item[b])
  }

  @Test
  fun `reading an item without the value never throws`() {
    assertNull(ItemStack(Material.AIR)[pdcKey<Int>(plugin, "a")])
  }

  @Test
  fun `property delegates persist on the holder`() {
    assertEquals(0, player.kills)
    player.kills++
    player.kills += 2
    assertEquals(3, player.kills)
    assertNull(player.nick)
    player.nick = "Bob"
    assertEquals("Bob", player.nick)
    player.nick = null
    assertNull(player.nick)
  }

  @Test
  fun `item property delegates`() {
    val item = ItemStack(Material.DIAMOND_SWORD)
    assertEquals(1, item.level)
    item.level = 5
    assertEquals(5, item.level)
  }

  @Test
  fun `block editPdc returns false for plain blocks`() {
    val world = server.addSimpleWorld("w")
    val block = world.getBlockAt(0, 64, 0).also { it.type = Material.STONE }
    assertFalse(block.editPdc { })
  }
}
