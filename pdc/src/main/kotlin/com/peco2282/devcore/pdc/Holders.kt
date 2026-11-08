package com.peco2282.devcore.pdc

import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder

// ---- Holders: Entity, Player, Chunk, World, TileState, ... -------------------------------

/** Reads [key] from this holder's container. */
operator fun <T : Any> PersistentDataHolder.get(key: PdcKey<T>): T? = persistentDataContainer[key]

/** Writes [key] to this holder's container. */
operator fun <T : Any> PersistentDataHolder.set(key: PdcKey<T>, value: T) {
  persistentDataContainer[key] = value
}

/** Whether this holder stores [key]. */
operator fun PersistentDataHolder.contains(key: PdcKey<*>): Boolean = key in persistentDataContainer

/** Removes [key] from this holder. */
fun PersistentDataHolder.remove(key: PdcKey<*>) = persistentDataContainer.remove(key)

// ---- ItemStack -----------------------------------------------------------------------------

/**
 * Edits this item's container in place. Item meta is a copy, so it has to be written back;
 * this does that for you. Returns the item for chaining.
 *
 * ```kotlin
 * item.editPdc { this[OWNER] = player.uniqueId }
 * ```
 * @throws IllegalStateException if the item has no meta (on a real server, air).
 */
fun ItemStack.editPdc(edit: PersistentDataContainer.() -> Unit): ItemStack {
  val meta = itemMeta ?: throw IllegalStateException("$type cannot hold persistent data")
  meta.persistentDataContainer.edit()
  itemMeta = meta
  return this
}

/** Reads [key] from this item, or `null` if absent (or the item has no meta). */
operator fun <T : Any> ItemStack.get(key: PdcKey<T>): T? = itemMeta?.persistentDataContainer?.get(key)

/** Writes [key] to this item. */
operator fun <T : Any> ItemStack.set(key: PdcKey<T>, value: T) {
  editPdc { this[key] = value }
}

/** Whether this item stores [key]. */
operator fun ItemStack.contains(key: PdcKey<*>): Boolean = itemMeta?.persistentDataContainer?.contains(key) == true

/** Removes [key] from this item. */
fun ItemStack.removePdc(key: PdcKey<*>) {
  editPdc { remove(key) }
}

// ---- Blocks --------------------------------------------------------------------------------

/**
 * Edits the container of a block that can store data (chests, signs, furnaces, ... — a [TileState]).
 *
 * Plain blocks such as stone cannot hold data; store it on the chunk instead
 * (`block.chunk[KEY]`, keyed by position).
 *
 * @return `false` if the block cannot hold data (nothing was changed).
 */
fun Block.editPdc(edit: PersistentDataContainer.() -> Unit): Boolean {
  val state = state as? TileState ?: return false
  state.persistentDataContainer.edit()
  state.update()
  return true
}
