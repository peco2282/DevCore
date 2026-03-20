package com.peco2282.devcore.world

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.inventory.ItemStack

@WorldDsl
interface BlockEditor {
  val block: Block

  /**
   * ブロックの種類を設定します。
   */
  var type: Material

  /**
   * ブロックデータを設定します。
   */
  var blockData: BlockData

  /**
   * アイテムをドロップします。
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
