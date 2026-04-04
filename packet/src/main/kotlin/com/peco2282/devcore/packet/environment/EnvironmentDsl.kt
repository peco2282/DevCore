package com.peco2282.devcore.packet.environment

import com.peco2282.devcore.packet.PacketAPI
import org.bukkit.entity.Player

@DslMarker
annotation class PacketEnvironmentDsl

/**
 * 環境偽装DSLのビルダークラス。
 */
@PacketEnvironmentDsl
class EnvironmentBuilder(private val player: Player) {

  /**
   * 偽の天候を設定する。
   * @param rain 雨を降らせるか
   * @param thunder 雷雨にするか
   */
  fun weather(rain: Boolean, thunder: Boolean = false) {
    PacketAPI.setFakeWeather(player, rain, thunder)
  }

  /**
   * 偽の時刻を設定する。
   * @param time ゲーム内時刻 (0-24000)
   * @param locked 時刻を固定するか
   */
  fun time(time: Long, locked: Boolean = false) {
    PacketAPI.setFakeTime(player, time, locked)
  }

  /**
   * 偽のバイオームを設定し、霧・空の色を変える。
   * @param biomeKey バイオームのリソースキー (例: "minecraft:nether_wastes")
   */
  fun biome(biomeKey: String) {
    PacketAPI.setFakeBiome(player, biomeKey)
  }

  /**
   * 偽のワールドボーダーを表示する。
   */
  fun worldBorder(builder: FakeWorldBorderBuilder.() -> Unit) {
    PacketAPI.setFakeWorldBorder(player, builder)
  }

  /**
   * ワールドボーダーをリセットする。
   */
  fun resetWorldBorder() {
    PacketAPI.resetWorldBorder(player)
  }

  /**
   * 雨・雷の強度を設定する。
   * @param rainLevel 雨の強度 (0.0-1.0)
   * @param thunderLevel 雷の強度 (0.0-1.0)
   */
  fun weatherLevel(rainLevel: Float, thunderLevel: Float = 0f) {
    PacketAPI.setWeatherLevel(player, rainLevel, thunderLevel)
  }
}

/**
 * プレイヤーの環境を偽装するDSLエントリポイント。
 *
 * ```kotlin
 * player.environment {
 *   weather(rain = true, thunder = true)
 *   time(18000L) // 夕焼け
 *   biome("minecraft:nether_wastes")
 * }
 * ```
 */
fun Player.environment(action: EnvironmentBuilder.() -> Unit) {
  EnvironmentBuilder(this).apply(action)
}
