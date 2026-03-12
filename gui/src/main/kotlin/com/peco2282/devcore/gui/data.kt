package com.peco2282.devcore.gui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * インベントリイベントのコンテキスト
 * @property player クリックしたプレイヤー
 * @property slot クリックされたスロット番号
 * @property bukkitEvent Bukkitの元のイベント
 */
data class GuiClickEvent(
  val player: Player,
  val slot: Int,
  val bukkitEvent: InventoryClickEvent
) {
  /**
   * クリックされたアイテムを取得します。
   */
  val currentItem: ItemStack? get() = bukkitEvent.currentItem
}
