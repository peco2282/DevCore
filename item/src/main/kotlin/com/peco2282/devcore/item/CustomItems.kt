package com.peco2282.devcore.item

import com.peco2282.devcore.pdc.PdcKey
import com.peco2282.devcore.pdc.get
import com.peco2282.devcore.pdc.pdcKey
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

/**
 * The custom items of one plugin.
 *
 * ```kotlin
 * val items = plugin.customItems()
 *
 * val ruby = items.register("ruby_sword", Material.DIAMOND_SWORD) {
 *   appearance { displayName = Component.text("Ruby Sword"); enchant(Enchantment.SHARPNESS, 3) }
 *   onAttack { it.damage *= 1.5 }
 *   shaped(" R", " R", " S") { ingredient('R', Material.REDSTONE); ingredient('S', Material.STICK) }
 * }
 * ruby.give(player)
 * ```
 */
class CustomItems internal constructor(val plugin: Plugin) {
  private val items = ConcurrentHashMap<String, CustomItem>()
  private val idKey: PdcKey<String> = pdcKey(plugin, "custom_item")
  private val recipeKeys = mutableListOf<NamespacedKey>()
  private val listener = ItemListener()
  private var closed = false

  init {
    Bukkit.getPluginManager().registerEvents(listener, plugin)
  }

  /**
   * Defines an item and registers its recipes.
   * @throws IllegalArgumentException for a duplicate or invalid [id].
   */
  fun register(id: String, material: Material, definition: CustomItemBuilder.() -> Unit = {}): CustomItem {
    check(!closed) { "This registry was closed" }
    require(id.matches(ID)) { "Invalid id '$id': use lowercase letters, digits and . _ - /" }
    require(!material.isAir && material.isItem) { "$material is not an item" }
    val builder = CustomItemBuilder(this, material).apply(definition)
    val item = CustomItem(id, material, idKey, builder.appearanceBlock(), builder.behavior())
    require(items.putIfAbsent(id, item) == null) { "Custom item '$id' is already registered" }
    registerRecipes(item, builder.recipes)
    return item
  }

  operator fun get(id: String): CustomItem? = items[id]

  /** All registered items. */
  val all: Collection<CustomItem> get() = items.values.toList()

  /** The id stored on [stack], or `null` if it is not one of this registry's items. */
  fun idOf(stack: ItemStack?): String? =
    if (stack == null || stack.type.isAir) null else stack[idKey]?.takeIf { items.containsKey(it) }

  /** The item [stack] is an instance of, if any. */
  fun of(stack: ItemStack?): CustomItem? = idOf(stack)?.let(items::get)

  /** Removes the recipes and stops handling events. Existing stacks stay valid and keep their id. */
  fun close() {
    if (closed) return
    closed = true
    HandlerList.unregisterAll(listener)
    recipeKeys.forEach { Bukkit.removeRecipe(it) }
    recipeKeys.clear()
  }

  private fun registerRecipes(item: CustomItem, defs: List<RecipeDefinition>) {
    defs.forEachIndexed { index, def ->
      val key = if (index == 0) item.recipeKey else NamespacedKey(item.recipeKey.namespace, "${item.recipeKey.key}_${index + 1}")
      val result = item.create(def.amount)
      val recipe = when (def) {
        is RecipeDefinition.Shaped -> ShapedRecipe(key, result).apply {
          shape(*def.rows.toTypedArray())
          def.choices.forEach { (symbol, choice) -> setIngredient(symbol, choice) }
        }
        is RecipeDefinition.Shapeless -> ShapelessRecipe(key, result).apply {
          def.choices.forEach { addIngredient(it) }
        }
      }
      if (Bukkit.addRecipe(recipe)) recipeKeys += key
      else plugin.logger.warning("Recipe ${key} for custom item '${item.id}' was not added (duplicate or invalid)")
    }
  }

  private inner class ItemListener : Listener {
    private fun run(label: String, block: () -> Unit) {
      try {
        block()
      } catch (e: Exception) {
        plugin.logger.severe("Custom item $label handler failed: ${e.message}")
        e.printStackTrace()
      }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onInteract(event: PlayerInteractEvent) {
      if (event.hand != EquipmentSlot.HAND) return
      val item = of(event.item) ?: return
      val handlers = when (event.action) {
        Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> item.behavior.onRightClick
        Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> item.behavior.onLeftClick
        else -> return
      }
      handlers.forEach { run(item.id) { it(event) } }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onAttack(event: EntityDamageByEntityEvent) {
      val player = event.damager as? Player ?: return
      val item = of(player.inventory.itemInMainHand) ?: return
      item.behavior.onAttack.forEach { run(item.id) { it(event) } }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onBreak(event: BlockBreakEvent) {
      val item = of(event.player.inventory.itemInMainHand) ?: return
      item.behavior.onBreak.forEach { run(item.id) { it(event) } }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlace(event: BlockPlaceEvent) {
      val item = of(event.itemInHand) ?: return
      if (!item.behavior.placeable) {
        event.isCancelled = true
        return
      }
      item.behavior.onPlace.forEach { run(item.id) { it(event) } }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onConsume(event: PlayerItemConsumeEvent) {
      val item = of(event.item) ?: return
      item.behavior.onConsume.forEach { run(item.id) { it(event) } }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onDrop(event: PlayerDropItemEvent) {
      val item = of(event.itemDrop.itemStack) ?: return
      item.behavior.onDrop.forEach { run(item.id) { it(event) } }
    }
  }

  private companion object {
    val ID = Regex("[a-z0-9._/-]+")
  }
}

/** Creates the custom item registry for this plugin. Call once, typically in `onEnable`. */
fun Plugin.customItems(block: CustomItems.() -> Unit = {}): CustomItems = CustomItems(this).apply(block)

/** Convenience: `ItemStack` → id string stored under this registry, or `null`. */
fun CustomItems.isCustom(stack: ItemStack?): Boolean = of(stack) != null
