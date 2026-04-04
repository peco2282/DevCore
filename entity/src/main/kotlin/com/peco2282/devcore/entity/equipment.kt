package com.peco2282.devcore.entity

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.ItemStack

/**
 * A DSL builder for modifying the equipment of a living entity.
 *
 * Provides convenient properties for getting and setting each equipment slot.
 *
 * @param equipment The [EntityEquipment] instance to modify
 */
@EntityDsl
class EquipmentBuilder(private val equipment: EntityEquipment) {
  /** The item worn in the helmet slot, or null if empty. */
  var helmet: ItemStack?
    get() = equipment.helmet
    set(value) {
      equipment.helmet = value
    }

  /** The item worn in the chestplate slot, or null if empty. */
  var chestplate: ItemStack?
    get() = equipment.chestplate
    set(value) {
      equipment.chestplate = value
    }

  /** The item worn in the leggings slot, or null if empty. */
  var leggings: ItemStack?
    get() = equipment.leggings
    set(value) {
      equipment.leggings = value
    }

  /** The item worn in the boots slot, or null if empty. */
  var boots: ItemStack?
    get() = equipment.boots
    set(value) {
      equipment.boots = value
    }

  /** The item held in the main hand, or null if empty. */
  var itemInMainHand: ItemStack?
    get() = equipment.itemInMainHand
    set(value) {
      equipment.setItemInMainHand(value)
    }

  /** The item held in the off hand, or null if empty. */
  var itemInOffHand: ItemStack?
    get() = equipment.itemInOffHand
    set(value) {
      equipment.setItemInOffHand(value)
    }
}

/**
 * Modifies the equipment of this entity using a DSL builder.
 *
 * This function only applies to [LivingEntity] instances that have equipment.
 * If the entity has no equipment slot (e.g., some mobs), the block is not executed.
 *
 * Example usage:
 * ```
 * entity.equipment {
 *   helmet = ItemStack(Material.DIAMOND_HELMET)
 *   itemInMainHand = ItemStack(Material.DIAMOND_SWORD)
 * }
 * ```
 *
 * @param editor A lambda with receiver that configures the entity's equipment
 */
fun Entity.equipment(editor: EquipmentBuilder.() -> Unit) {
  if (this is LivingEntity) {
    this.equipment?.let { EquipmentBuilder(it).apply(editor) }
  }
}
