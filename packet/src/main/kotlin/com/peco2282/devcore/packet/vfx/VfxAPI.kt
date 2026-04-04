package com.peco2282.devcore.packet.vfx

import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

@DslMarker
annotation class PacketVfxDsl

/**
 * 高度な視覚効果に関するパケット操作のインターフェース。
 * ブロック破壊クラック・装備偽装・音響エフェクトのローカライズを制御する。
 */
interface VfxHub {
  /**
   * 特定プレイヤーにだけブロックのひび割れ（破壊クラック）を表示する。
   * @param player 対象プレイヤー
   * @param location ひび割れを表示するブロックの座標
   * @param stage ひび割れの段階 (0-9、-1で非表示)
   */
  fun setBlockCrack(player: Player, location: Location, stage: Int)

  /**
   * 特定プレイヤーにだけエンティティの装備を偽装する。
   * @param player 対象プレイヤー
   * @param entityId 対象エンティティID
   * @param slot 装備スロット
   * @param item 偽装するアイテム
   */
  fun fakeEquipment(player: Player, entityId: Int, slot: EquipmentSlot, item: ItemStack)

  /**
   * 特定プレイヤーにだけエンティティの燃焼エフェクトを表示・非表示にする。
   * @param player 対象プレイヤー
   * @param entityId 対象エンティティID
   * @param onFire 燃焼エフェクトを表示するか
   */
  fun setEntityOnFire(player: Player, entityId: Int, onFire: Boolean)

  /**
   * 特定プレイヤーにだけ偽の爆発エフェクトを表示する（地形破壊なし）。
   * @param player 対象プレイヤー
   * @param location 爆発の座標
   * @param power 爆発の大きさ
   */
  fun fakeExplosion(player: Player, location: Location, power: Float)

  /**
   * 特定プレイヤーにだけ偽の雷エフェクトを表示・音を再生する。
   * @param player 対象プレイヤー
   * @param location 雷の座標
   */
  fun fakeLightning(player: Player, location: Location)

  /**
   * 特定プレイヤーにだけ偽の音を再生する。
   * @param player 対象プレイヤー
   * @param sound 再生するサウンド
   * @param location 再生する座標
   * @param volume 音量
   * @param pitch ピッチ
   */
  fun localSound(player: Player, sound: Sound, location: Location, volume: Float = 1f, pitch: Float = 1f)
}
