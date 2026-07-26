package com.peco2282.devcore.entity

import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.entity.Player

fun Player.sendTitle(title: Component, subTitle: Component = Component.empty(), fadeInSecond: Float = 1F, staySecond: Float = 1F, fadeOutSecond: Float = 1F) {
  fun asTick(sec: Float) = Ticks.duration((sec * 20).toLong())
  val times = Title.Times.times(
    asTick(fadeInSecond),
    asTick(staySecond),
    asTick(fadeOutSecond)
  )
  showTitle(
    Title.title(
      title,
      subTitle,
      times
    )
  )
}
