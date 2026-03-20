package com.peco2282.devcore.world

import org.bukkit.World

/**
 * 時刻をDSLで設定します。
 */
fun World.time(time: Long) {
  edit { this.time = time }
}

/**
 * 天気をDSLで設定します。
 */
fun World.weather(type: WeatherType) {
  edit { weather = type }
}
