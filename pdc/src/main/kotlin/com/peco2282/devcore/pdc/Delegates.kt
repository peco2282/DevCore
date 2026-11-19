package com.peco2282.devcore.pdc

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.plugin.Plugin
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Stores a property in the holder's container, so the value survives restarts.
 *
 * Declare it as an extension property; the delegate is created once and shared by all holders.
 *
 * ```kotlin
 * var Player.kills: Int by pdc(plugin, "kills", default = 0)
 *
 * player.kills++            // persisted
 * ```
 */
inline fun <reified T : Any> pdc(plugin: Plugin, name: String, default: T): ReadWriteProperty<PersistentDataHolder, T> =
  HolderProperty(pdcKey(plugin, name)) { default }

/** Like [pdc], but the default is computed on each read of an absent value. */
inline fun <reified T : Any> pdc(plugin: Plugin, name: String, noinline default: () -> T): ReadWriteProperty<PersistentDataHolder, T> =
  HolderProperty(pdcKey(plugin, name), default)

/**
 * A nullable persisted property. Assigning `null` removes the stored value.
 *
 * ```kotlin
 * var Player.nickname: String? by pdcOrNull(plugin, "nickname")
 * ```
 */
inline fun <reified T : Any> pdcOrNull(plugin: Plugin, name: String): ReadWriteProperty<PersistentDataHolder, T?> =
  NullableHolderProperty(pdcKey(plugin, name))

/** [pdc] with an explicit [key], for when no plugin instance is available at declaration time. */
inline fun <reified T : Any> pdc(key: NamespacedKey, default: T): ReadWriteProperty<PersistentDataHolder, T> =
  HolderProperty(pdcKey(key)) { default }

/** [pdcOrNull] with an explicit [key]. */
inline fun <reified T : Any> pdcOrNull(key: NamespacedKey): ReadWriteProperty<PersistentDataHolder, T?> =
  NullableHolderProperty(pdcKey(key))

/** [itemPdc] with an explicit [key]. */
inline fun <reified T : Any> itemPdc(key: NamespacedKey, default: T): ReadWriteProperty<ItemStack, T> =
  ItemProperty(pdcKey(key)) { default }

/** [pdc] for [ItemStack] receivers. */
inline fun <reified T : Any> itemPdc(plugin: Plugin, name: String, default: T): ReadWriteProperty<ItemStack, T> =
  ItemProperty(pdcKey(plugin, name)) { default }

/** [pdcOrNull] for [ItemStack] receivers. */
inline fun <reified T : Any> itemPdcOrNull(plugin: Plugin, name: String): ReadWriteProperty<ItemStack, T?> =
  NullableItemProperty(pdcKey(plugin, name))

@PublishedApi
internal class HolderProperty<T : Any>(private val key: PdcKey<T>, private val default: () -> T) :
  ReadWriteProperty<PersistentDataHolder, T> {
  override fun getValue(thisRef: PersistentDataHolder, property: KProperty<*>): T = thisRef[key] ?: default()
  override fun setValue(thisRef: PersistentDataHolder, property: KProperty<*>, value: T) {
    thisRef[key] = value
  }
}

@PublishedApi
internal class NullableHolderProperty<T : Any>(private val key: PdcKey<T>) :
  ReadWriteProperty<PersistentDataHolder, T?> {
  override fun getValue(thisRef: PersistentDataHolder, property: KProperty<*>): T? = thisRef[key]
  override fun setValue(thisRef: PersistentDataHolder, property: KProperty<*>, value: T?) {
    if (value == null) thisRef.remove(key) else thisRef[key] = value
  }
}

@PublishedApi
internal class ItemProperty<T : Any>(private val key: PdcKey<T>, private val default: () -> T) :
  ReadWriteProperty<ItemStack, T> {
  override fun getValue(thisRef: ItemStack, property: KProperty<*>): T = thisRef[key] ?: default()
  override fun setValue(thisRef: ItemStack, property: KProperty<*>, value: T) {
    thisRef[key] = value
  }
}

@PublishedApi
internal class NullableItemProperty<T : Any>(private val key: PdcKey<T>) : ReadWriteProperty<ItemStack, T?> {
  override fun getValue(thisRef: ItemStack, property: KProperty<*>): T? = thisRef[key]
  override fun setValue(thisRef: ItemStack, property: KProperty<*>, value: T?) {
    if (value == null) thisRef.removePdc(key) else thisRef[key] = value
  }
}
