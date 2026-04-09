@file:Suppress("UnstableApiUsage")

package com.peco2282.devcore.util

import io.papermc.paper.datacomponent.DataComponentType
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun ItemStack.editor() = ItemEditorImpl(this)
fun Material.editor() = ItemEditorImpl(this)

fun item(target: Material, editor: ItemEditor.() -> Unit) {
  ItemEditorImpl(target).apply(editor)
}

fun item(target: ItemStack, editor: ItemEditor.() -> Unit) {
  ItemEditorImpl(target).apply(editor)
}

interface DataComponentEditor {
  fun data(type: DataComponentType, value: Any): DataComponentEditor

}

interface ItemEditor {
  var amount: Int
  var durability: Short
  var name: Component
  var lore: List<Component>

  infix fun lore(lore: List<Component>)
  infix fun displayName(name: Component)
  fun data(editor: DataComponentEditor.() -> Unit)
}

internal class DataComponentEditorImpl : DataComponentEditor {
  private val types: MutableMap<DataComponentType, Any> = mutableMapOf()

  override fun data(type: DataComponentType, value: Any) = apply {
    types[type] = value
  }
}

class ItemEditorImpl(val target: Material) : ItemEditor {
  constructor(target: ItemStack) : this(target.type)

  init {
    require(target != Material.AIR) { "Cannot create item editor with AIR material" }
  }

  override var amount: Int by this::amount
  override var durability: Short by this::durability
  override var name: Component by this::name
  override var lore: List<Component> = emptyList()

  fun build(): ItemStack = ItemStack(target, amount).apply {
    itemMeta = itemMeta?.apply {
      displayName(name)
      lore(this@ItemEditorImpl.lore)
    }
  }

  override infix fun lore(lore: List<Component>) {
    this.lore = lore
  }

  override infix fun displayName(name: Component) {
    this.name = name
  }

  override fun data(editor: DataComponentEditor.() -> Unit) {
    DataComponentEditorImpl().apply(editor)
  }
}