package com.peco2282.devcore.packet

import com.github.retrooper.packetevents.protocol.player.EquipmentSlot
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KProperty

/**
 * A builder for managing entity equipment using a DSL.
 * This class uses delegated properties to efficiently store only the set items.
 *
 * Example usage:
 * ```kotlin
 * equipment {
 *     mainHand = ItemStack(Material.DIAMOND_SWORD)
 *     helmet = ItemStack(Material.IRON_HELMET)
 * }
 * ```
 */
@FakeVisualDsl
class EquipmentBuilder {
  /**
   * Internal map holding set equipment.
   */
  private val _items = mutableMapOf<EquipmentSlot, ItemStack>()
  internal val items: Map<EquipmentSlot, ItemStack> get() = _items

  /**
   * The item in the main hand.
   */
  var mainHand: ItemStack? by EquipmentSlot.MAIN_HAND.delegate()

  /**
   * The item in the off hand.
   */
  var offHand: ItemStack? by EquipmentSlot.OFF_HAND.delegate()

  /**
   * The helmet item.
   */
  var helmet: ItemStack? by EquipmentSlot.HELMET.delegate()

  /**
   * The chestplate item.
   */
  var chestplate: ItemStack? by EquipmentSlot.CHEST_PLATE.delegate()

  /**
   * The leggings item.
   */
  var leggings: ItemStack? by EquipmentSlot.LEGGINGS.delegate()

  /**
   * The boots item.
   */
  var boots: ItemStack? by EquipmentSlot.BOOTS.delegate()

  /**
   * Internal delegate class for equipment slots.
   * Assigning null or AIR material removes the entry from the internal map to save memory.
   */
  private inner class EquipmentMapDelegate(val slot: EquipmentSlot) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): ItemStack? = _items[slot]
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: ItemStack?) {
      if (value == null || value.type == Material.AIR) {
        _items.remove(slot)
      } else {
        _items[slot] = value
      }
    }
  }

  /**
   * Extension function to create a delegate for an [EquipmentSlot].
   */
  private fun EquipmentSlot.delegate() = EquipmentMapDelegate(this)
}