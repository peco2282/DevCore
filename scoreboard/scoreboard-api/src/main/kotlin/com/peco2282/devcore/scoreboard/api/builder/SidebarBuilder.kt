package com.peco2282.devcore.scoreboard.api.builder

import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scheduler.ticks
import com.peco2282.devcore.scoreboard.api.ScoreboardApi
import com.peco2282.devcore.scoreboard.api.SidebarHandle
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * DSL marker for Scoreboard-related builders.
 */
@DslMarker
annotation class ScoreboardDsl

/**
 * A builder class for creating [SidebarHandle] instances using a DSL.
 *
 * @param title A function providing the initial title of the sidebar.
 */
@ScoreboardDsl
class SidebarBuilder(private var title: () -> Component) {
  private val lines = mutableListOf<(Player) -> Component>()
  private var refreshInterval: Ticks? = null
  private var plugin: Plugin? = null

  /**
   * Sets a static title for the sidebar.
   */
  infix fun title(title: Component) = apply {
    this.title = { title }
  }

  /**
   * Sets a dynamic title for the sidebar.
   */
  infix fun title(title: () -> Component) = apply {
    this.title = title
  }

  /**
   * Adds a static line to the sidebar.
   */
  infix fun line(line: Component) = apply {
    lines.add { line }
  }

  /**
   * Adds a dynamic line to the sidebar.
   */
  infix fun line(line: () -> Component) = apply {
    lines.add { line() }
  }

  /**
   * Adds a line that can change based on the player.
   */
  infix fun line(line: (Player) -> Component) = apply {
    lines.add(line)
  }

  /**
   * Adds multiple lines using the [LinesBuilder] DSL.
   */
  infix fun lines(block: LinesBuilder.() -> Unit) = apply {
    val builder = LinesBuilder().apply(block)
    lines.addAll(builder.build())
  }

  /**
   * Enables automatic refreshing of the sidebar.
   *
   * @param plugin The plugin instance to run the task.
   * @param interval The refresh interval (default: 20 ticks).
   */
  fun autoRefresh(plugin: Plugin, interval: Ticks = 20.ticks) = apply {
    this.plugin = plugin
    this.refreshInterval = interval
  }

  /**
   * Builds and registers the [SidebarHandle].
   */
  fun build(): SidebarHandle {
    return ScoreboardApi.factory().createSidebar(title, lines.toList(), plugin, refreshInterval).also {
      ScoreboardApi.register(it)
    }
  }
}

/**
 * A builder class for defining multiple lines in a sidebar using a more concise DSL.
 */
@ScoreboardDsl
class LinesBuilder {
  private val lines = mutableListOf<(Player) -> Component>()

  /**
   * Adds a static string as a line.
   */
  operator fun String.unaryPlus() {
    lines.add { Component.text(this) }
  }

  /**
   * Adds a static component as a line.
   */
  operator fun Component.unaryPlus() {
    lines.add { this }
  }

  /**
   * Adds a line that changes based on the player.
   */
  operator fun ((Player) -> Component).unaryPlus() {
    lines.add(this)
  }

  /**
   * Adds a string-returning function as a line.
   */
  @JvmName("plusStringFunction")
  operator fun ((Player) -> String).unaryPlus() {
    lines.add { Component.text(this(it)) }
  }

  /**
   * Adds a component-returning function as a line.
   */
  operator fun (() -> Component).unaryPlus() {
    lines.add { this() }
  }

  /**
   * Adds a string-returning function as a line.
   */
  @JvmName("plusStringNoArgFunction")
  operator fun (() -> String).unaryPlus() {
    lines.add { Component.text(this()) }
  }

  /**
   * Returns the list of line functions.
   */
  fun build() = lines.toList()
}
