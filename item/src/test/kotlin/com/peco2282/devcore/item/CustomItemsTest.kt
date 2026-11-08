package com.peco2282.devcore.item

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.entity.Cow
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.block.BlockFace
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CustomItemsTest {
  private lateinit var server: ServerMock
  private lateinit var plugin: Plugin
  private lateinit var player: Player
  private lateinit var items: CustomItems
  private val plain = PlainTextComponentSerializer.plainText()

  private companion object { var next = 0 }

  @BeforeEach
  fun setUp() {
    server = MockBukkit.mock()
    plugin = MockBukkit.createMockPlugin("items-${next++}")
    player = server.addPlayer()
    items = plugin.customItems()
  }

  @AfterEach
  fun tearDown() = MockBukkit.unmock()

  private fun hold(stack: ItemStack) = player.inventory.setItemInMainHand(stack)
  private fun fire(e: org.bukkit.event.Event) = server.pluginManager.callEvent(e)

  @Test
  fun `create applies the appearance and tags the stack`() {
    val ruby = items.register("ruby", Material.DIAMOND) {
      appearance { displayName = Component.text("Ruby") }
    }
    val stack = ruby.create(3)
    assertEquals(Material.DIAMOND, stack.type)
    assertEquals(3, stack.amount)
    assertEquals("Ruby", plain.serialize(stack.itemMeta!!.displayName()!!))
    assertTrue(ruby.matches(stack))
    assertSame(ruby, items.of(stack))
  }

  @Test
  fun `identification survives renaming and cloning, plain items do not match`() {
    val ruby = items.register("ruby", Material.DIAMOND)
    val renamed = ruby.create().also { s -> s.editMeta { it.displayName(Component.text("Something else")) } }
    assertTrue(ruby.matches(renamed))
    assertTrue(ruby.matches(renamed.clone()))
    assertFalse(ruby.matches(ItemStack(Material.DIAMOND)))
    assertFalse(ruby.matches(null))
    assertNull(items.idOf(ItemStack(Material.AIR)))
  }

  @Test
  fun `items from another registry are not recognized`() {
    val other = MockBukkit.createMockPlugin("other-${next++}").customItems()
    val foreign = other.register("ruby", Material.DIAMOND).create()
    val mine = items.register("ruby", Material.DIAMOND)
    assertFalse(mine.matches(foreign))
  }

  @Test
  fun `duplicate and invalid ids are rejected`() {
    items.register("a", Material.STONE)
    assertFailsWith<IllegalArgumentException> { items.register("a", Material.STONE) }
    assertFailsWith<IllegalArgumentException> { items.register("Bad Id", Material.STONE) }
    assertFailsWith<IllegalArgumentException> { items.register("air", Material.AIR) }
  }

  @Test
  fun `create rejects impossible amounts`() {
    val sword = items.register("sword", Material.DIAMOND_SWORD)
    assertFailsWith<IllegalArgumentException> { sword.create(2) }
    assertFailsWith<IllegalArgumentException> { sword.create(0) }
  }

  @Test
  fun `right and left click handlers only fire for this item and the main hand`() {
    var right = 0; var left = 0
    val wand = items.register("wand", Material.STICK) {
      onRightClick { right++ }
      onLeftClick { left++ }
    }
    val stack = wand.create()
    fire(PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, stack, null, BlockFace.SELF))
    fire(PlayerInteractEvent(player, Action.LEFT_CLICK_AIR, stack, null, BlockFace.SELF))
    fire(PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, ItemStack(Material.STICK), null, BlockFace.SELF))
    fire(PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, stack, null, BlockFace.SELF, EquipmentSlot.OFF_HAND))
    fire(PlayerInteractEvent(player, Action.PHYSICAL, stack, null, BlockFace.SELF))
    assertEquals(1, right)
    assertEquals(1, left)
  }

  @Test
  fun `attack handler can change the damage`() {
    val blade = items.register("blade", Material.IRON_SWORD) { onAttack { it.damage = it.damage * 2 } }
    hold(blade.create())
    val cow = player.world.spawn(player.location, Cow::class.java)
    val event = EntityDamageByEntityEvent(player, cow, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 5.0)
    fire(event)
    assertEquals(10.0, event.damage)
  }

  @Test
  fun `attack with another item is ignored`() {
    var hits = 0
    items.register("blade", Material.IRON_SWORD) { onAttack { hits++ } }
    hold(ItemStack(Material.IRON_SWORD))
    val cow = player.world.spawn(player.location, Cow::class.java)
    fire(EntityDamageByEntityEvent(player, cow, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 5.0))
    assertEquals(0, hits)
  }

  @Test
  fun `break consume and drop handlers fire`() {
    var broke = 0; var ate = 0; var dropped = 0
    val tool = items.register("tool", Material.GOLDEN_PICKAXE) { onBreak { broke++ } }
    val food = items.register("food", Material.APPLE) { onConsume { ate++ } }
    val junk = items.register("junk", Material.STICK) { onDrop { dropped++ } }

    val world = server.addSimpleWorld("w")
    hold(tool.create())
    fire(BlockBreakEvent(world.getBlockAt(0, 64, 0), player))
    fire(PlayerItemConsumeEvent(player, food.create()))
    val entity = world.dropItem(player.location, junk.create())
    fire(PlayerDropItemEvent(player, entity))
    assertEquals(listOf(1, 1, 1), listOf(broke, ate, dropped))
  }

  @Test
  fun `non placeable items cancel block placement`() {
    val dirt = items.register("magic_dirt", Material.DIRT) { placeable = false }
    val world = server.addSimpleWorld("w2")
    val block = world.getBlockAt(0, 64, 0)
    val stack = dirt.create()
    val event = BlockPlaceEvent(block, block.state, world.getBlockAt(0, 63, 0), stack, player, true, EquipmentSlot.HAND)
    fire(event)
    assertTrue(event.isCancelled)

    val plain = items.register("plain_dirt", Material.DIRT)
    val ok = BlockPlaceEvent(block, block.state, world.getBlockAt(0, 63, 0), plain.create(), player, true, EquipmentSlot.HAND)
    fire(ok)
    assertFalse(ok.isCancelled)
  }

  @Test
  fun `a throwing handler does not break other handlers or the event`() {
    var second = 0
    val wand = items.register("wand", Material.STICK) {
      onRightClick { error("boom") }
      onRightClick { second++ }
    }
    fire(PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, wand.create(), null, BlockFace.SELF))
    assertEquals(1, second)
  }

  @Test
  fun `give puts items in the inventory and drops the overflow`() {
    val gem = items.register("gem", Material.EMERALD)
    assertTrue(gem.give(player, 70))
    assertEquals(70, player.inventory.contents.filterNotNull().filter { gem.matches(it) }.sumOf { it.amount })
    player.inventory.clear()
    for (slot in 0 until player.inventory.size) player.inventory.setItem(slot, ItemStack(Material.STONE, 64))
    assertFalse(gem.give(player, 1))
  }

  @Test
  fun `recipes are registered with the registry and removed on close`() {
    val axe = items.register("axe", Material.IRON_AXE) {
      shaped("II", "IS", " S") {
        ingredient('I', Material.IRON_INGOT)
        ingredient('S', Material.STICK)
      }
      shapeless { ingredient(Material.IRON_INGOT, 2) }
    }
    assertNotNull(server.getRecipe(axe.recipeKey))
    items.close()
    assertNull(server.getRecipe(axe.recipeKey))
  }

  @Test
  fun `recipe definitions are validated`() {
    assertFailsWith<IllegalArgumentException> {
      items.register("a", Material.STICK) { shaped("AB") { ingredient('A', Material.STONE) } }
    }
    assertFailsWith<IllegalArgumentException> {
      items.register("b", Material.STICK) { shaped("A") { ingredient('Z', Material.STONE) } }
    }
    assertFailsWith<IllegalArgumentException> { items.register("c", Material.STICK) { shaped("AAAA") { } } }
    assertFailsWith<IllegalArgumentException> { items.register("d", Material.STICK) { shapeless { } } }
  }

  @Test
  fun `closed registry stops handling events`() {
    var clicks = 0
    val wand = items.register("wand", Material.STICK) { onRightClick { clicks++ } }
    items.close()
    fire(PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, wand.create(), null, BlockFace.SELF))
    assertEquals(0, clicks)
    assertFailsWith<IllegalStateException> { items.register("x", Material.STONE) }
  }
}
