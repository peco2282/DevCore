package com.peco2282.devcore.packet.view

import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

@DslMarker
annotation class PacketViewDsl

/**
 * 視覚的ハックに関するパケット操作のインターフェース。
 * カメラ・発光フィルタ・エンティティ変換を個人単位で制御する。
 */
interface ViewHub {
  /**
   * プレイヤーの視点を特定エンティティに強制固定する（シネマティックカメラ）。
   * @param player 対象プレイヤー
   * @param entityId 視点を固定するエンティティID
   */
  fun setCameraEntity(player: Player, entityId: Int)

  /**
   * プレイヤーの視点を自分自身に戻す。
   * @param player 対象プレイヤー
   */
  fun resetCamera(player: Player)

  /**
   * 特定プレイヤーにだけエンティティを発光させる。
   * @param player 対象プレイヤー
   * @param entityId 発光させるエンティティID
   * @param glowing 発光させるか
   */
  fun setEntityGlowing(player: Player, entityId: Int, glowing: Boolean)

  /**
   * 特定プレイヤーにだけエンティティの見た目を別の種類に変える。
   * @param player 対象プレイヤー
   * @param entityId 変換するエンティティID
   * @param type 見せかけのエンティティタイプ
   */
  fun transformEntityType(player: Player, entityId: Int, type: EntityType)

  /**
   * 特定プレイヤーにだけエンティティのスケールを変える。
   * @param player 対象プレイヤー
   * @param entityId 対象エンティティID
   * @param scale スケール倍率
   */
  fun setEntityScale(player: Player, entityId: Int, scale: Float)

  /**
   * 特定プレイヤーにだけエンティティを逆さまに表示する。
   * @param player 対象プレイヤー
   * @param entityId 対象エンティティID
   * @param upsideDown 逆さまにするか
   */
  fun setEntityUpsideDown(player: Player, entityId: Int, upsideDown: Boolean)
}
