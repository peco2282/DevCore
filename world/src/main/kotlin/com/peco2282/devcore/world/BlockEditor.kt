package com.peco2282.devcore.world

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.inventory.ItemStack

/**
 * Interface for editing block properties in a DSL-style manner.
 *
 * This interface provides a fluent API for modifying blocks in the world.
 * It is designed to be used within a DSL context, typically within a lambda
 * passed to a world or location editing function.
 *
 * Example usage:
 * ```kotlin
 * // Basic block type change
 * blockEditor {
 *   type = Material.DIAMOND_BLOCK
 * }
 *
 * // Modifying block data
 * blockEditor {
 *   type = Material.CHEST
 *   blockData = (blockData as Directional).apply {
 *     facing = BlockFace.NORTH
 *   }
 * }
 *
 * // Dropping items
 * blockEditor {
 *   dropItem(ItemStack(Material.DIAMOND, 5))
 *   type = Material.AIR
 * }
 * ```
 */
@WorldDsl
interface BlockEditor {
  /**
   * The underlying Bukkit [Block] being edited.
   */
  val block: Block

  /**
   * The material type of the block.
   *
   * Setting this property changes the block's type in the world.
   *
   * Example:
   * ```kotlin
   * blockEditor {
   *   type = Material.STONE
   * }
   * ```
   */
  var type: Material

  /**
   * The block data containing additional state information.
   *
   * This property allows you to access and modify block-specific data
   * such as rotation, waterlogging, orientation, etc.
   *
   * Example:
   * ```kotlin
   * blockEditor {
   *   type = Material.STAIRS
   *   blockData = (blockData as Stairs).apply {
   *     half = Bisected.Half.TOP
   *     facing = BlockFace.EAST
   *   }
   * }
   * ```
   */
  var blockData: BlockData

  /**
   * Drops an item naturally at the block's location.
   *
   * The item will be spawned at the block's location as if it was
   * dropped naturally (with random velocity).
   *
   * @param itemStack The item stack to drop.
   *
   * Example:
   * ```kotlin
   * blockEditor {
   *   dropItem(ItemStack(Material.EMERALD, 3))
   *   dropItem(ItemStack(Material.GOLD_INGOT, 1))
   * }
   * ```
   */
  fun dropItem(itemStack: ItemStack)
}

internal class BlockEditorImpl(override val block: Block) : BlockEditor {
  override var type: Material
    get() = block.type
    set(value) {
      block.type = value
    }

  override var blockData: BlockData
    get() = block.blockData
    set(value) {
      block.blockData = value
    }

  override fun dropItem(itemStack: ItemStack) {
    block.world.dropItemNaturally(block.location, itemStack)
  }
}
