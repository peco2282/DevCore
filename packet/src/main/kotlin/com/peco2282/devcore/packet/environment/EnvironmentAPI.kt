package com.peco2282.devcore.packet.environment

import org.bukkit.entity.Player

/**
 * 環境偽装に関するパケット操作のインターフェース。
 * 天候・時刻・バイオーム・ワールドボーダーを個人単位で制御する。
 */
interface EnvironmentHub {
  /**
   * 特定プレイヤーに偽の天候を送信する。
   * @param player 対象プレイヤー
   * @param rain 雨を降らせるか
   * @param thunder 雷雨にするか
   */
  fun setFakeWeather(player: Player, rain: Boolean, thunder: Boolean)

  /**
   * 特定プレイヤーに偽の時刻を送信する。
   * @param player 対象プレイヤー
   * @param time ゲーム内時刻 (0-24000)
   * @param locked 時刻を固定するか
   */
  fun setFakeTime(player: Player, time: Long, locked: Boolean = false)

  /**
   * 特定プレイヤーに偽のバイオームを送信し、霧・空の色を変える。
   * @param player 対象プレイヤー
   * @param biomeKey バイオームのリソースキー (例: "minecraft:nether_wastes")
   */
  fun setFakeBiome(player: Player, biomeKey: String)

  /**
   * 特定プレイヤーに偽のワールドボーダーを表示する。
   * @param player 対象プレイヤー
   * @param builder ワールドボーダーの設定
   */
  fun setFakeWorldBorder(player: Player, builder: FakeWorldBorderBuilder.() -> Unit)

  /**
   * 特定プレイヤーのワールドボーダーをリセットする。
   * @param player 対象プレイヤー
   */
  fun resetWorldBorder(player: Player)

  /**
   * 雨・雷の強度を設定する。
   * @param player 対象プレイヤー
   * @param rainLevel 雨の強度 (0.0-1.0)
   * @param thunderLevel 雷の強度 (0.0-1.0)
   */
  fun setWeatherLevel(player: Player, rainLevel: Float, thunderLevel: Float)
}

/**
 * 偽のワールドボーダー設定ビルダー。
 */
interface FakeWorldBorderBuilder {
  /** ボーダーの中心X座標 */
  var centerX: Double
  /** ボーダーの中心Z座標 */
  var centerZ: Double
  /** ボーダーのサイズ（直径） */
  var size: Double
  /** 変化前のサイズ（アニメーション用） */
  var oldSize: Double
  /** サイズ変化にかかる時間（ミリ秒） */
  var lerpTime: Long
  /** 警告距離（ブロック数） */
  var warningBlocks: Int
  /** 警告時間（秒） */
  var warningTime: Int
}
