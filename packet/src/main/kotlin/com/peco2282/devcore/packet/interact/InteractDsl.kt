package com.peco2282.devcore.packet.interact

import com.peco2282.devcore.packet.PacketAPI
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 偽のUI・操作系DSLのビルダークラス。
 */
@PacketInteractDsl
class InteractBuilder(private val player: Player) {

  /**
   * 偽のブロックを設置し、インタラクションを傍受できるようにする。
   * @param location 偽ブロックの座標
   * @param material 偽ブロックの種類
   */
  fun fakeBlock(location: Location, material: Material) {
    PacketAPI.placeFakeBlock(player, location, material)
  }

  /**
   * 偽のブロックを削除する。
   * @param location 削除する偽ブロックの座標
   */
  fun removeFakeBlock(location: Location) {
    PacketAPI.removeFakeBlock(player, location)
  }

  /**
   * インベントリスロットに偽のアイテムを固定表示する。
   * @param slot スロット番号
   * @param item 表示するアイテム（nullで非表示）
   */
  fun lockSlot(slot: Int, item: ItemStack?) {
    PacketAPI.lockInventorySlot(player, slot, item)
  }

  /**
   * ホットバースロット選択を強制変更する。
   * @param slot ホットバースロット番号 (0-8)
   */
  fun forceHeldSlot(slot: Int) {
    PacketAPI.forceHeldSlot(player, slot)
  }

  /**
   * クレジット画面（エンドロール）を表示する。
   */
  fun showCredits() {
    PacketAPI.showCredits(player)
  }

  /**
   * クレジット画面を非表示にする。
   */
  fun hideCredits() {
    PacketAPI.hideCredits(player)
  }
}

/**
 * プレイヤーのUI・操作を偽装するDSLエントリポイント。
 *
 * ```kotlin
 * player.interact {
 *   fakeBlock(location, Material.CHEST)
 *   lockSlot(4, ItemStack(Material.BARRIER))
 *   showCredits()
 * }
 * ```
 */
fun Player.interact(action: InteractBuilder.() -> Unit) {
  InteractBuilder(this).apply(action)
}
