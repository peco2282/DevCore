package com.peco2282.devcore.display

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.plugin.Plugin

/** A group of text displays owned by one plugin. */
class Hologram internal constructor(
  private val lifecycle: DisplayLifecycle,
  private val rows: MutableList<Row>,
) {
  internal data class Row(val index: Int, val viewer: Player?, val display: TextDisplay)

  /** Replaces every rendered copy of the line at [index]. */
  fun update(index: Int, render: () -> Component) {
    require(rows.any { it.index == index }) { "No hologram line at index $index" }
    val component = render()
    rows.filter { it.index == index }.forEach { it.display.text(component) }
  }

  /** Re-renders the line at [index] for each of its viewers. */
  fun updateForViewers(index: Int, render: (Player) -> Component) {
    require(rows.any { it.index == index }) { "No hologram line at index $index" }
    rows.filter { it.index == index }.forEach { row -> row.viewer?.let { row.display.text(render(it)) } }
  }

  /** Removes all backing entities. This operation is idempotent. */
  fun remove() {
    rows.toList().forEach { row -> row.display.remove(); lifecycle.forget(row.display) }
    rows.clear()
  }
}

/** Creates a lifecycle-owned hologram at [location]. */
fun Plugin.hologram(location: Location, block: HologramBuilder.() -> Unit): Hologram {
  val world = requireNotNull(location.world) { "Hologram location must have a world" }
  val spec = HologramBuilder().apply(block).buildSpec()
  val lifecycle = DisplayLifecycle.of(this)
  val selectedViewers = spec.viewers.ifEmpty { server.onlinePlayers.toSet() }
  val rows = mutableListOf<Hologram.Row>()
  val offsets = hologramLineOffsets(spec.lines.size, spec.lineSpacing)

  spec.lines.forEachIndexed { index, line ->
    val rowLocation = location.clone().add(0.0, offsets[index], 0.0)
    when (line) {
      is HologramLine.Static -> {
        val display = world.spawn(rowLocation, TextDisplay::class.java) { entity ->
          configure(entity, line.component, spec)
          if (spec.viewers.isNotEmpty()) entity.isVisibleByDefault = false
        }
        spec.viewers.forEach { it.showEntity(this, display) }
        lifecycle.track(display)
        rows += Hologram.Row(index, null, display)
      }
      is HologramLine.PerViewer -> selectedViewers.forEach { viewer ->
        val display = world.spawn(rowLocation, TextDisplay::class.java) { entity ->
          configure(entity, line.render(viewer), spec)
          entity.isVisibleByDefault = false
        }
        viewer.showEntity(this, display)
        lifecycle.track(display)
        rows += Hologram.Row(index, viewer, display)
      }
    }
  }
  return Hologram(lifecycle, rows)
}

private fun configure(display: TextDisplay, text: Component, spec: HologramSpec) {
  display.text(text)
  display.billboard = spec.billboard
  display.lineWidth = spec.lineWidth
  display.isPersistent = false
}
