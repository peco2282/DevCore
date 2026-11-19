package com.peco2282.devcore.display

import net.kyori.adventure.text.Component
import org.bukkit.entity.Display
import org.bukkit.entity.Player

/** Content source for one hologram row. */
sealed interface HologramLine {
  data class Static(val component: Component) : HologramLine
  data class PerViewer(val render: (Player) -> Component) : HologramLine
}

internal data class HologramSpec(
  val lines: List<HologramLine>,
  val billboard: Display.Billboard,
  val lineSpacing: Double,
  val lineWidth: Int,
  val viewers: Set<Player>,
)

/** Collects the content and rendering options for a hologram. */
class HologramBuilder {
  private val content = mutableListOf<HologramLine>()
  private val viewers = linkedSetOf<Player>()

  var billboard: Display.Billboard = Display.Billboard.CENTER
  var lineSpacing: Double = 0.28
  var lineWidth: Int = 200

  /** Adds content shared by every viewer. */
  fun line(component: Component) { content += HologramLine.Static(component) }
  /** Adds content rendered separately for each selected viewer. */
  fun line(render: (Player) -> Component) { content += HologramLine.PerViewer(render) }
  /** Restricts this hologram to the supplied players. */
  fun visibleTo(vararg player: Player) { viewers += player }

  internal fun buildSpec(): HologramSpec {
    require(lineSpacing.isFinite() && lineSpacing >= 0.0) { "Line spacing must be finite and non-negative" }
    require(lineWidth > 0) { "Line width must be positive" }
    return HologramSpec(content.toList(), billboard, lineSpacing, lineWidth, viewers.toSet())
  }
}

/** Returns downward Y offsets for [count] hologram lines. */
fun hologramLineOffsets(count: Int, spacing: Double): List<Double> {
  require(count >= 0) { "Line count must be non-negative" }
  require(spacing.isFinite() && spacing >= 0.0) { "Line spacing must be finite and non-negative" }
  return List(count) { -it * spacing }
}
