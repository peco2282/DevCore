package com.peco2282.devcore.util

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack


/**
 * Counts the total amount of items of the specified material type in this inventory.
 *
 * @param material The material type to count
 * @return The total amount of items of the specified material
 */
fun Inventory.countItem(material: Material): Int = contents.filterNotNull().filter { it.type == material }.sumOf { it.amount }

/**
 * Checks if this inventory contains at least the specified amount of the given material.
 *
 * @param material The material type to check for
 * @param amount The minimum amount required (default is 1)
 * @return `true` if the inventory contains at least the specified amount, `false` otherwise
 */
fun Inventory.hasItem(material: Material, amount: Int = 1): Boolean = countItem(material) >= amount

/**
 * Safely removes the specified amount of items of the given material from this inventory.
 *
 * This method only removes items if the inventory contains enough of the specified material.
 * It iterates through inventory slots and removes items until the specified amount is removed.
 *
 * @param material The material type to remove
 * @param amount The amount to remove
 * @return `true` if the items were successfully removed, `false` if there were not enough items
 */
fun Inventory.removeItemSafely(material: Material, amount: Int): Boolean {
  if (!hasItem(material, amount)) return false

  var remaining = amount
  for (i in 0 until size) {
    val item = getItem(i) ?: continue
    if (item.type == material) {
      if (item.amount <= remaining) {
        remaining -= item.amount
        setItem(i, null)
      } else {
        item.amount -= remaining
        remaining = 0
      }
    }
    if (remaining <= 0) break
  }
  return true
}

/**
 * Checks if this inventory is completely full (no empty slots).
 *
 * @return `true` if the inventory has no empty slots, `false` otherwise
 */
fun Inventory.isFull(): Boolean = firstEmpty() == -1

/**
 * Adds multiple items to this inventory and returns any items that couldn't be added.
 *
 * @param items The items to add to the inventory
 * @return A list of ItemStacks that couldn't be added due to the inventory being full
 */
fun Inventory.addItems(vararg items: ItemStack): List<ItemStack> = addItem(*items).values.toList()
