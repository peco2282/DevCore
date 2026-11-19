package com.peco2282.devcore.item

import com.peco2282.devcore.pdc.editPdc
import com.peco2282.devcore.pdc.get
import com.peco2282.devcore.pdc.PdcKey
import com.peco2282.devcore.pdc.set
import com.peco2282.devcore.util.ItemStackBuilder
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * An item type you defined: a base [material], an appearance, and behavior.
 *
 * Stacks made by [create] carry a hidden id, so they are recognized no matter what the player
 * renames or relores them to, and behavior follows the item instead of its name.
 */
class CustomItem internal constructor(
  /** Unique within its registry. Lowercase letters, digits and `. _ - /`. */
  val id: String,
  val material: Material,
  internal val key: PdcKey<String>,
  internal val appearance: ItemStackBuilder.() -> Unit,
  internal val behavior: Behavior
) {
  /** The key used for this item's recipes. */
  val recipeKey: NamespacedKey = NamespacedKey(key.key.namespace, "recipe/$id")

  /** Creates a new stack of [amount]. */
  fun create(amount: Int = 1): ItemStack {
    require(amount in 1..material.maxStackSize.coerceAtLeast(1)) {
      "amount $amount is outside 1..${material.maxStackSize} for $material"
    }
    val stack = ItemStackBuilder(material, amount).apply(appearance).build()
    return stack.editPdc { this[key] = id }
  }

  /** Whether [stack] is an instance of this item. */
  fun matches(stack: ItemStack?): Boolean = stack != null && !stack.type.isAir && stack[key] == id

  /**
   * Gives [amount] to [player]. Whatever does not fit is dropped at their feet.
   * @return `true` if everything fit in the inventory.
   */
  fun give(player: Player, amount: Int = 1): Boolean {
    var remaining = amount
    var allFit = true
    while (remaining > 0) {
      val batch = minOf(remaining, material.maxStackSize.coerceAtLeast(1))
      val leftover = player.inventory.addItem(create(batch))
      if (leftover.isNotEmpty()) {
        allFit = false
        leftover.values.forEach { player.world.dropItemNaturally(player.location, it) }
      }
      remaining -= batch
    }
    return allFit
  }

  override fun toString(): String = "CustomItem($id)"
}
