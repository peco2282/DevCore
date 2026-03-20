package com.peco2282.devcore.world

import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

@WorldDsl
interface PlayerEditor {
  val player: Player

  /**
   * ゲームモードを設定します。
   */
  var gameMode: GameMode

  /**
   * 体力を設定します。
   */
  var health: Double

  /**
   * 満腹度を設定します。
   */
  var foodLevel: Int

  /**
   * 経験値を設定します。
   */
  var exp: Float

  /**
   * レベルを設定します。
   */
  var level: Int

  /**
   * インベントリを取得します。
   */
  val inventory: Inventory

  /**
   * メッセージを送信します。
   */
  fun sendMessage(message: Component)

  /**
   * メッセージを送信します。
   */
  fun sendMessage(message: String)

  /**
   * 指定した場所にテレポートします。
   */
  fun teleport(location: Location)
}

internal class PlayerEditorImpl(override val player: Player) : PlayerEditor {
  override var gameMode: GameMode
    get() = player.gameMode
    set(value) {
      player.gameMode = value
    }

  override var health: Double
    get() = player.health
    set(value) {
      player.health = value
    }

  override var foodLevel: Int
    get() = player.foodLevel
    set(value) {
      player.foodLevel = value
    }

  override var exp: Float
    get() = player.exp
    set(value) {
      player.exp = value
    }

  override var level: Int
    get() = player.level
    set(value) {
      player.level = value
    }

  override val inventory: Inventory
    get() = player.inventory

  override fun sendMessage(message: Component) {
    player.sendMessage(message)
  }

  override fun sendMessage(message: String) {
    player.sendMessage(Component.text(message))
  }

  override fun teleport(location: Location) {
    player.teleport(location)
  }
}

/**
 * プレイヤーの設定をDSLで編集します。
 */
fun Player.edit(action: PlayerEditor.() -> Unit) {
  PlayerEditorImpl(this).apply(action)
}
