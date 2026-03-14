package com.peco2282.devcore.scoreboard.api.builder

import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scheduler.ticks
import com.peco2282.devcore.scoreboard.api.ScoreboardApi
import com.peco2282.devcore.scoreboard.api.SidebarHandle
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

@DslMarker
annotation class ScoreboardDsl

@ScoreboardDsl
class SidebarBuilder(private val title: () -> Component) {
  private val lines = mutableListOf<(Player) -> Component>()
  private var refreshInterval: Ticks? = null
  private var plugin: Plugin? = null

  fun line(line: Component) = apply {
    lines.add { line }
  }

  infix fun line(line: () -> Component) = apply {
    lines.add { line() }
  }

  infix fun line(line: (Player) -> Component) = apply {
    lines.add(line)
  }

  infix fun lines(block: LinesBuilder.() -> Unit) = apply {
    val builder = LinesBuilder().apply(block)
    lines.addAll(builder.build())
  }

  fun autoRefresh(plugin: Plugin, interval: Ticks = 20.ticks) = apply {
    this.plugin = plugin
    this.refreshInterval = interval
  }

  fun build(): SidebarHandle {
    return ScoreboardApi.factory().createSidebar(title, lines.toList(), plugin, refreshInterval).also {
      ScoreboardApi.register(it)
    }
  }
}

@ScoreboardDsl
class LinesBuilder {
  private val lines = mutableListOf<(Player) -> Component>()

  operator fun String.unaryPlus() {
    lines.add { Component.text(this) }
  }

  operator fun Component.unaryPlus() {
    lines.add { this }
  }

  operator fun ((Player) -> Component).unaryPlus() {
    lines.add(this)
  }

  @JvmName("plusStringFunction")
  operator fun ((Player) -> String).unaryPlus() {
    lines.add { Component.text(this(it)) }
  }

  operator fun (() -> Component).unaryPlus() {
    lines.add { this() }
  }

  @JvmName("plusStringNoArgFunction")
  operator fun (() -> String).unaryPlus() {
    lines.add { Component.text(this()) }
  }

  fun build() = lines.toList()
}
