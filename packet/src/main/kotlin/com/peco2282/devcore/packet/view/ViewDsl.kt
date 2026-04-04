package com.peco2282.devcore.packet.view

import com.peco2282.devcore.packet.PacketAPI
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

/**
 * 視覚的ハックDSLのビルダークラス。
 */
@PacketViewDsl
class ViewBuilder(private val player: Player) {

  /**
   * プレイヤーの視点を特定エンティティに強制固定する（シネマティックカメラ）。
   * @param entityId 視点を固定するエンティティID
   */
  fun camera(entityId: Int) {
    PacketAPI.setCameraEntity(player, entityId)
  }

  /**
   * プレイヤーの視点を自分自身に戻す。
   */
  fun resetCamera() {
    PacketAPI.resetCamera(player)
  }

  /**
   * 特定プレイヤーにだけエンティティを発光させる。
   * @param entityId 発光させるエンティティID
   * @param glowing 発光させるか
   */
  fun glow(entityId: Int, glowing: Boolean = true) {
    PacketAPI.setEntityGlowing(player, entityId, glowing)
  }

  /**
   * 特定プレイヤーにだけエンティティの見た目を別の種類に変える。
   * @param entityId 変換するエンティティID
   * @param type 見せかけのエンティティタイプ
   */
  fun transformType(entityId: Int, type: EntityType) {
    PacketAPI.transformEntityType(player, entityId, type)
  }

  /**
   * 特定プレイヤーにだけエンティティのスケールを変える。
   * @param entityId 対象エンティティID
   * @param scale スケール倍率
   */
  fun scale(entityId: Int, scale: Float) {
    PacketAPI.setEntityScale(player, entityId, scale)
  }

  /**
   * 特定プレイヤーにだけエンティティを逆さまに表示する。
   * @param entityId 対象エンティティID
   * @param upsideDown 逆さまにするか
   */
  fun upsideDown(entityId: Int, upsideDown: Boolean = true) {
    PacketAPI.setEntityUpsideDown(player, entityId, upsideDown)
  }
}

/**
 * プレイヤーの視覚を操作するDSLエントリポイント。
 *
 * ```kotlin
 * player.view {
 *   camera(droneEntity.entityId)
 *   glow(targetEntity.entityId, true)
 *   scale(bossEntity.entityId, 3.0f)
 * }
 * ```
 */
fun Player.view(action: ViewBuilder.() -> Unit) {
  ViewBuilder(this).apply(action)
}
