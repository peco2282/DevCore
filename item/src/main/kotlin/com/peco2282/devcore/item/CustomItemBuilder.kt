package com.peco2282.devcore.item

import com.peco2282.devcore.util.ItemStackBuilder
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice

@DslMarker
annotation class CustomItemDsl

/** DSL receiver for [CustomItems.register]. */
@CustomItemDsl
class CustomItemBuilder internal constructor(private val registry: CustomItems, private val material: Material) {
  private var appearance: ItemStackBuilder.() -> Unit = {}
  private val rightClick = mutableListOf<(PlayerInteractEvent) -> Unit>()
  private val leftClick = mutableListOf<(PlayerInteractEvent) -> Unit>()
  private val attack = mutableListOf<(EntityDamageByEntityEvent) -> Unit>()
  private val breakBlock = mutableListOf<(BlockBreakEvent) -> Unit>()
  private val place = mutableListOf<(BlockPlaceEvent) -> Unit>()
  private val consume = mutableListOf<(PlayerItemConsumeEvent) -> Unit>()
  private val drop = mutableListOf<(PlayerDropItemEvent) -> Unit>()
  internal val recipes = mutableListOf<RecipeDefinition>()

  /**
   * Whether the item can be placed when its [material] is a block.
   * Set `false` for items that only look like a block (e.g. a "magic dirt" that is not meant to be built with).
   */
  var placeable: Boolean = true

  /** Name, lore, enchantments, model data and so on. Applied to every stack created. */
  fun appearance(block: ItemStackBuilder.() -> Unit) {
    appearance = block
  }

  /** Right-click in the air or on a block with this item in the main hand. */
  fun onRightClick(handler: (PlayerInteractEvent) -> Unit) { rightClick += handler }

  /** Left-click in the air or on a block with this item in the main hand. */
  fun onLeftClick(handler: (PlayerInteractEvent) -> Unit) { leftClick += handler }

  /** A player hits an entity with this item in the main hand. */
  fun onAttack(handler: (EntityDamageByEntityEvent) -> Unit) { attack += handler }

  /** A player breaks a block with this item in the main hand. */
  fun onBreak(handler: (BlockBreakEvent) -> Unit) { breakBlock += handler }

  /** A player places this item as a block. */
  fun onPlace(handler: (BlockPlaceEvent) -> Unit) { place += handler }

  /** A player eats or drinks this item. */
  fun onConsume(handler: (PlayerItemConsumeEvent) -> Unit) { consume += handler }

  /** A player drops this item. */
  fun onDrop(handler: (PlayerDropItemEvent) -> Unit) { drop += handler }

  /**
   * A shaped crafting recipe.
   *
   * ```kotlin
   * shaped("DD", " S", " S") {
   *   ingredient('D', Material.DIAMOND)
   *   ingredient('S', Material.STICK)
   * }
   * ```
   */
  fun shaped(vararg rows: String, amount: Int = 1, block: ShapedBuilder.() -> Unit) {
    require(rows.isNotEmpty() && rows.size <= 3 && rows.all { it.length in 1..3 }) {
      "A shaped recipe needs 1-3 rows of 1-3 characters"
    }
    val builder = ShapedBuilder(rows.toList()).apply(block)
    val used = rows.joinToString("").filter { it != ' ' }.toSet()
    require(used.all { it in builder.choices }) {
      "No ingredient for ${used.filter { it !in builder.choices }}"
    }
    recipes += RecipeDefinition.Shaped(rows.toList(), builder.choices.toMap(), amount)
  }

  /** A shapeless crafting recipe. */
  fun shapeless(amount: Int = 1, block: ShapelessBuilder.() -> Unit) {
    val builder = ShapelessBuilder().apply(block)
    require(builder.choices.isNotEmpty() && builder.choices.size <= 9) { "A shapeless recipe needs 1-9 ingredients" }
    recipes += RecipeDefinition.Shapeless(builder.choices.toList(), amount)
  }

  internal fun behavior() = Behavior(
    rightClick.toList(), leftClick.toList(), attack.toList(), breakBlock.toList(),
    place.toList(), consume.toList(), drop.toList(), placeable
  )

  internal fun appearanceBlock() = appearance
}

/** Ingredient choices for a shaped recipe, by character. */
@CustomItemDsl
class ShapedBuilder internal constructor(rows: List<String>) {
  internal val choices = mutableMapOf<Char, RecipeChoice>()
  private val valid = rows.joinToString("").toSet()

  fun ingredient(symbol: Char, material: Material) = put(symbol, RecipeChoice.MaterialChoice(material))

  /** Requires exactly this item (including its name and data), e.g. another custom item's [CustomItem.create]. */
  fun ingredient(symbol: Char, exact: ItemStack) = put(symbol, RecipeChoice.ExactChoice(exact))

  private fun put(symbol: Char, choice: RecipeChoice) {
    require(symbol != ' ') { "' ' means an empty slot" }
    require(symbol in valid) { "'$symbol' does not appear in the recipe shape" }
    choices[symbol] = choice
  }
}

/** Ingredients for a shapeless recipe. */
@CustomItemDsl
class ShapelessBuilder internal constructor() {
  internal val choices = mutableListOf<RecipeChoice>()

  fun ingredient(material: Material, count: Int = 1) {
    repeat(count) { choices += RecipeChoice.MaterialChoice(material) }
  }

  fun ingredient(exact: ItemStack, count: Int = 1) {
    repeat(count) { choices += RecipeChoice.ExactChoice(exact) }
  }
}

internal sealed interface RecipeDefinition {
  val amount: Int

  class Shaped(val rows: List<String>, val choices: Map<Char, RecipeChoice>, override val amount: Int) : RecipeDefinition
  class Shapeless(val choices: List<RecipeChoice>, override val amount: Int) : RecipeDefinition
}
