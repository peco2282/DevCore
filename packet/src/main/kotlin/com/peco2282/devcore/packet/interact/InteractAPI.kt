package com.peco2282.devcore.packet.interact

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@DslMarker
annotation class PacketInteractDsl

/**
 * 偽のUI・操作系に関するパケット操作のインターフェース。
 * 偽ブロック操作・インベントリスロットロック・クレジット演出を制御する。
 */
interface InteractHub {
  /**
   * 特定プレイヤーに偽のブロックを設置し、インタラクションを傍受できるようにする。
   * @param player 対象プレイヤー
   * @param location 偽ブロックの座標
   * @param material 偽ブロックの種類
   */
  fun placeFakeBlock(player: Player, location: Location, material: Material)

  /**
   * 特定プレイヤーの偽ブロックを削除する。
   * @param player 対象プレイヤー
   * @param location 削除する偽ブロックの座標
   */
  fun removeFakeBlock(player: Player, location: Location)

  /**
   * 特定プレイヤーのインベントリスロットに偽のアイテムを固定表示する。
   * @param player 対象プレイヤー
   * @param slot スロット番号
   * @param item 表示するアイテム（nullで非表示）
   */
  fun lockInventorySlot(player: Player, slot: Int, item: ItemStack?)

  /**
   * 特定プレイヤーのホットバースロット選択を強制変更する。
   * @param player 対象プレイヤー
   * @param slot ホットバースロット番号 (0-8)
   */
  fun forceHeldSlot(player: Player, slot: Int)

  /**
   * クレジット画面（エンドロール）を任意のタイミングで表示する。
   * @param player 対象プレイヤー
   */
  fun showCredits(player: Player)

  /**
   * クレジット画面を非表示にする。
   * @param player 対象プレイヤー
   */
  fun hideCredits(player: Player)
}
