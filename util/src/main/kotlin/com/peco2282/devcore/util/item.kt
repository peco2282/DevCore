package com.peco2282.devcore.util

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * DSL marker annotation for ItemStack builder to prevent nested builder scopes.
 */
@DslMarker
annotation class ItemDsl

/**
 * A DSL builder for creating and modifying ItemStacks in a declarative way.
 *
 * This builder provides a type-safe way to configure ItemStack properties including:
 * - Display name and lore
 * - Enchantments
 * - Item flags
 * - Attributes
 * - Custom model data
 *
 * Example usage:
 * ```kotlin
 * val sword = itemStack(Material.DIAMOND_SWORD) {
 *     displayName = Component.text("Legendary Sword")
 *     lore(
 *         Component.text("A powerful weapon"),
 *         Component.text("Forged in ancient times")
 *     )
 *     enchant(Enchantment.SHARPNESS, 5)
 *     hideAll()
 * }
 * ```
 *
 * @property item The ItemStack being modified
 * @constructor Creates a builder from an existing ItemStack
 * @throws IllegalStateException if the ItemStack's ItemMeta is null
 */
@ItemDsl
class ItemStackBuilder(private val item: ItemStack) {
  private val meta: ItemMeta = item.itemMeta ?: throw IllegalStateException("ItemMeta is null for ${item.type}")

  /**
   * Creates a builder for a new ItemStack with the specified material and amount.
   *
   * @param material The material type for the ItemStack
   * @param amount The stack size (default: 1)
   */
  constructor(material: Material, amount: Int = 1) : this(ItemStack(material, amount))

  /**
   * The material type of the ItemStack.
   */
  var material: Material
    get() = item.type
    set(value) {
      item.type = value
    }

  /**
   * The stack size of the ItemStack.
   */
  var amount: Int
    get() = item.amount
    set(value) {
      item.amount = value
    }

  /**
   * The display name of the ItemStack as an Adventure Component.
   * Set to null to remove the display name.
   */
  var displayName: Component?
    get() = meta.displayName()
    set(value) {
      meta.displayName(value)
    }

  /**
   * The lore lines of the ItemStack as a list of Adventure Components.
   * Set to null to remove all lore.
   */
  var lore: List<Component>?
    get() = meta.lore()
    set(value) {
      meta.lore(value)
    }

  /**
   * The custom model data value for resource pack model overrides.
   * Returns null if no custom model data is set.
   */
  var customModelData: Int?
    get() = if (meta.hasCustomModelData()) meta.customModelData else null
    set(value) {
      meta.setCustomModelData(value)
    }

  /**
   * Sets the lore from vararg Component parameters.
   *
   * @param lines The lore lines to set
   */
  fun lore(vararg lines: Component) {
    lore = lines.toList()
  }

  /**
   * Sets the lore from an iterable collection of Components.
   *
   * @param lines The lore lines to set
   */
  fun lore(lines: Iterable<Component>) {
    lore = lines.toList()
  }

  /**
   * Adds item flags to hide specific attributes or information.
   *
   * @param flags The ItemFlags to add
   */
  fun flags(vararg flags: ItemFlag) {
    meta.addItemFlags(*flags)
  }

  /**
   * Hides all possible item information by adding all ItemFlags.
   */
  fun hideAll() {
    flags(*ItemFlag.entries.toTypedArray())
  }

  /**
   * Adds an enchantment to the ItemStack.
   *
   * @param enchantment The enchantment to add
   * @param level The enchantment level (default: 1)
   * @param ignoreLevelRestriction Whether to ignore max level restrictions (default: true)
   */
  fun enchant(enchantment: Enchantment, level: Int = 1, ignoreLevelRestriction: Boolean = true) {
    meta.addEnchant(enchantment, level, ignoreLevelRestriction)
  }

  /**
   * Adds multiple enchantments from pairs of Enchantment to level.
   *
   * @param enchants Pairs of (Enchantment, level) to add
   */
  fun enchants(vararg enchants: Pair<Enchantment, Int>) {
    enchants.forEach { (enchant, level) -> enchant(enchant, level) }
  }

  /**
   * Adds an attribute modifier to the ItemStack.
   *
   * @param attribute The attribute to modify
   * @param modifier The modifier to apply
   */
  fun attribute(attribute: Attribute, modifier: AttributeModifier) {
    meta.addAttributeModifier(attribute, modifier)
  }

  /**
   * Builds and returns the configured ItemStack.
   *
   * @return The configured ItemStack
   */
  fun build(): ItemStack {
    item.itemMeta = meta
    return item
  }
}

/**
 * Creates a new ItemStack using a DSL builder.
 *
 * Example:
 * ```kotlin
 * val item = itemStack(Material.DIAMOND, 64) {
 *     displayName = Component.text("Shiny Diamonds")
 *     lore(Component.text("Very valuable!"))
 *     enchant(Enchantment.FORTUNE, 3)
 * }
 * ```
 *
 * @param material The material type for the ItemStack
 * @param amount The stack size (default: 1)
 * @param action The builder configuration lambda
 * @return The configured ItemStack
 */
fun itemStack(material: Material, amount: Int = 1, action: ItemStackBuilder.() -> Unit): ItemStack {
  return ItemStackBuilder(material, amount).apply(action).build()
}

/**
 * Creates a modified copy of this ItemStack using a DSL builder.
 * The original ItemStack is not modified.
 *
 * Example:
 * ```kotlin
 * val original = ItemStack(Material.IRON_SWORD)
 * val modified = original.edit {
 *     displayName = Component.text("Modified Sword")
 *     enchant(Enchantment.SHARPNESS, 2)
 * }
 * ```
 *
 * @param action The builder configuration lambda
 * @return A new modified ItemStack
 */
fun ItemStack.edit(action: ItemStackBuilder.() -> Unit): ItemStack {
  val newItem = this.clone()
  return ItemStackBuilder(newItem).apply(action).build()
}
