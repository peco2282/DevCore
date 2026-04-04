package com.peco2282.devcore.packet.vfx

import com.peco2282.devcore.packet.PacketAPI
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * 高度な視覚効果DSLのビルダークラス。
 */
@PacketVfxDsl
class VfxBuilder(private val player: Player) {

  /**
   * ブロックのひび割れ（破壊クラック）を表示する。
   * @param location ひび割れを表示するブロックの座標
   * @param stage ひび割れの段階 (0-9、-1で非表示)
   */
  fun blockCrack(location: Location, stage: Int) {
    PacketAPI.setBlockCrack(player, location, stage)
  }

  /**
   * エンティティの装備を偽装する。
   * @param entityId 対象エンティティID
   * @param slot 装備スロット
   * @param item 偽装するアイテム
   */
  fun fakeEquipment(entityId: Int, slot: EquipmentSlot, item: ItemStack) {
    PacketAPI.fakeEquipment(player, entityId, slot, item)
  }

  /**
   * エンティティの燃焼エフェクトを表示・非表示にする。
   * @param entityId 対象エンティティID
   * @param onFire 燃焼エフェクトを表示するか
   */
  fun onFire(entityId: Int, onFire: Boolean = true) {
    PacketAPI.setEntityOnFire(player, entityId, onFire)
  }

  /**
   * 偽の爆発エフェクトを表示する（地形破壊なし）。
   * @param location 爆発の座標
   * @param power 爆発の大きさ
   */
  fun explosion(location: Location, power: Float = 1f) {
    PacketAPI.fakeExplosion(player, location, power)
  }

  /**
   * 偽の雷エフェクトを表示・音を再生する。
   * @param location 雷の座標
   */
  fun lightning(location: Location) {
    PacketAPI.fakeLightning(player, location)
  }

  /**
   * 特定の座標から聞こえる偽の音を再生する。
   * @param sound 再生するサウンド
   * @param location 再生する座標
   * @param volume 音量
   * @param pitch ピッチ
   */
  fun localSound(sound: Sound, location: Location, volume: Float = 1f, pitch: Float = 1f) {
    PacketAPI.localSound(player, sound, location, volume, pitch)
  }
}

/**
 * プレイヤーへの視覚効果を制御するDSLエントリポイント。
 *
 * ```kotlin
 * player.vfx {
 *   blockCrack(location, stage = 5)
 *   explosion(location, power = 2f)
 *   lightning(strikeLocation)
 *   localSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER, location)
 * }
 * ```
 */
fun Player.vfx(action: VfxBuilder.() -> Unit) {
  VfxBuilder(this).apply(action)
}
